<template>
  <div class="admin-page">
    <AdminPageHeader
      title="毛利统计"
      desc="账单收入减去工资支出，按成衣厂汇总。金额口径是筛选后的整个区间，不是当前这一页。"
      :count="filteredData.length"
      count-label="家成衣厂"
    />

    <div class="filter-card">
      <div class="filter-bar">
        <el-select
          v-model="data.factoryName"
          placeholder="全部成衣厂"
          clearable
          filterable
          class="filter-wide"
          @change="handleFilterChange"
          @clear="handleFilterChange"
        >
          <el-option
            v-for="item in factoryOptions"
            :key="item.factoryName"
            :label="item.factoryName"
            :value="item.factoryName"
          />
        </el-select>
        <div class="filter-dates">
          <el-date-picker
            v-model="data.startDate"
            type="date"
            format="YYYY/MM/DD"
            value-format="YYYY-MM-DD"
            placeholder="开始日期"
            class="filter-date"
            @change="handleFilterChange"
          />
          <span class="range-sep">→</span>
          <el-date-picker
            v-model="data.endDate"
            type="date"
            format="YYYY/MM/DD"
            value-format="YYYY-MM-DD"
            placeholder="结束日期"
            class="filter-date"
            @change="handleFilterChange"
          />
        </div>
        <div class="filter-actions">
          <span class="filter-hint">不选日期时默认看本月 1 号到今天</span>
          <el-button type="primary" @click="handleFilterChange">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </div>
      </div>
    </div>

    <!-- 汇总卡片：口径是「筛选后的整个区间」，不是当前这一页 -->
    <div class="stat-grid">
      <div class="stat-card">
        <span class="stat-label">账单收入</span>
        <span class="stat-value">{{ formatMoney(totalBill) }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">工资支出</span>
        <span class="stat-value">{{ formatMoney(totalSalary) }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">毛利</span>
        <span class="stat-value" :class="{ 'is-negative': Number(totalGross) < 0 }">
          {{ formatMoney(totalGross) }}
        </span>
      </div>
      <div class="stat-card">
        <span class="stat-label">毛利率</span>
        <span class="stat-value">{{ totalMarginRate }}</span>
      </div>
    </div>

    <div class="table-card is-fill">
      <el-table
        :data="pageData"
        height="100%"
        stripe
        highlight-current-row
        empty-text="这个条件下没有账单和工资记录"
      >
        <el-table-column prop="factoryName" label="成衣厂" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            <div class="entity-cell">
              <span class="entity-avatar">{{ (scope.row.factoryName || '?').slice(0, 1) }}</span>
              <span class="entity-name">{{ scope.row.factoryName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="billTotal" label="账单收入" align="right" min-width="140">
          <template #default="scope">
            <span class="money">{{ formatMoney(scope.row.billTotal) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="salaryTotal" label="工资支出" align="right" min-width="140">
          <template #default="scope">
            <span class="money">{{ formatMoney(scope.row.salaryTotal) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="grossProfit" label="毛利" align="right" min-width="140">
          <template #default="scope">
            <span class="money" :class="{ 'is-negative': Number(scope.row.grossProfit) < 0 }">
              {{ formatMoney(scope.row.grossProfit) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="毛利率" align="right" width="120">
          <template #default="scope">
            <span class="num">{{ marginRate(scope.row) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pager-row">
      <el-pagination
        background
        :total="filteredData.length"
        v-model:current-page="data.currentPage"
        v-model:page-size="data.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import AdminPageHeader from '../AdminPageHeader.vue'
import { formatMoney } from '../adminShared.js'
import { queryFactoryGrossProfit } from '../../../network/admin/index.js'

const data = reactive({
  factoryName: '',
  startDate: '',
  endDate: '',
  tableData: [],
  currentPage: 1,
  pageSize: 10
})

/**
 * 下拉选项直接来自接口返回的行：接口是 factory LEFT JOIN 账单/工资，
 * 所以每家在用的成衣厂都会有一行（没业务的那几行金额是 0），不用再单独请求一次厂列表。
 * 按中文拼音顺序排，选起来比按毛利排更好找。
 */
const factoryOptions = computed(() =>
  data.tableData
    .filter(row => row.factoryName)
    .map(row => ({ factoryName: row.factoryName }))
    .sort((a, b) => String(a.factoryName).localeCompare(String(b.factoryName), 'zh-Hans-CN'))
)

/** 选定某家厂就只看这一行；没选就全看。名称在库里唯一（factory_name 唯一键），可以按名字匹配。 */
const filteredData = computed(() => {
  if (!data.factoryName) return data.tableData
  return data.tableData.filter(row => row.factoryName === data.factoryName)
})

/** 当前页要显示的行；分页在前端切片，避免为了分页再造一个接口 */
const pageData = computed(() => {
  const start = (data.currentPage - 1) * data.pageSize
  return filteredData.value.slice(start, start + data.pageSize)
})

/** 汇总口径 = 筛选后的整个区间（不是当前页），筛选成一家厂时就是这家厂的数字 */
const totalBill = computed(() => sumField('billTotal'))
const totalSalary = computed(() => sumField('salaryTotal'))
const totalGross = computed(() => sumField('grossProfit'))

/** 整体毛利率：没有收入时显示「–」，避免除零出现 Infinity */
const totalMarginRate = computed(() => {
  if (!totalBill.value) return '–'
  return `${((totalGross.value / totalBill.value) * 100).toFixed(1)}%`
})

onMounted(fetchList)

async function fetchList() {
  const { data: res } = await queryFactoryGrossProfit(data.startDate || undefined, data.endDate || undefined)
  if (res.status !== 200) {
    ElMessage.error(res.message)
    data.tableData = []
    return
  }
  data.tableData = res.data || []
  clampCurrentPage()
}

function sumField(field) {
  return filteredData.value.reduce((sum, row) => sum + Number(row[field] || 0), 0)
}

function handleFilterChange() {
  data.currentPage = 1
  fetchList()
}

function resetFilter() {
  data.factoryName = ''
  data.startDate = ''
  data.endDate = ''
  handleFilterChange()
}

/** 换条件后行数可能变少，回到最后一页，避免停在空页上 */
function clampCurrentPage() {
  const maxPage = Math.max(1, Math.ceil(filteredData.value.length / data.pageSize))
  if (data.currentPage > maxPage) data.currentPage = maxPage
}

/**
 * 毛利率：没有收入时显示「–」，避免除零出现 Infinity。
 */
function marginRate(row) {
  const bill = Number(row.billTotal || 0)
  if (!bill) return '–'
  return `${((Number(row.grossProfit || 0) / bill) * 100).toFixed(1)}%`
}

</script>

<style scoped>
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
  gap: 12px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px 16px;
  background: #ffffff;
  border: 1px solid var(--admin-card-border);
  border-radius: 14px;
  box-shadow: 0 6px 20px rgba(30, 58, 112, 0.04);
}

.stat-label {
  font-size: 12.5px;
  color: var(--admin-text-secondary);
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.01em;
  color: #1f2a44;
  font-variant-numeric: tabular-nums;
}

.stat-value.is-negative {
  color: #f04438;
}

.range-sep {
  color: var(--admin-text-muted);
  font-size: 13px;
}

/* 表格吃掉剩余高度，内部自己滚，分页不会被行数顶下去 */
.table-card.is-fill {
  flex: 1 1 0;
  min-height: 320px;
  display: flex;
  flex-direction: column;
}
</style>
