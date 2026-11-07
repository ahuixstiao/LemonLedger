<template>
  <div class="grossProfit-container">
    <!-- 筛选条件：名称宽度跟其它列表页一致，日期框 145px -->
    <div class="grossProfit-toolbar">
      <el-select
        v-model="data.factoryName"
        placeholder="成衣厂名称"
        clearable
        filterable
        class="name-select"
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
      <el-date-picker
        v-model="data.startDate"
        type="date"
        format="YYYY/MM/DD"
        value-format="YYYY-MM-DD"
        placeholder="开始日期"
        class="date-picker"
        @change="handleFilterChange"
      />
      <el-date-picker
        v-model="data.endDate"
        type="date"
        format="YYYY/MM/DD"
        value-format="YYYY-MM-DD"
        placeholder="结束日期"
        class="date-picker"
        @change="handleFilterChange"
      />
      <el-button type="primary" @click="handleFilterChange">查询</el-button>
      <el-button type="warning" plain class="reset-btn" @click="resetFilter">重置</el-button>
      <span class="toolbar-tip">不选日期时默认看本月 1 号到今天</span>
    </div>

    <!-- 汇总卡片：口径是「筛选后的整个区间」，不是当前这一页 -->
    <div class="summary-row">
      <div class="summary-card">
        <span class="summary-label">账单收入</span>
        <span class="summary-value">{{ money(totalBill) }}</span>
      </div>
      <div class="summary-card">
        <span class="summary-label">工资支出</span>
        <span class="summary-value">{{ money(totalSalary) }}</span>
      </div>
      <div class="summary-card">
        <span class="summary-label">毛利</span>
        <span class="summary-value" :class="{ negative: Number(totalGross) < 0 }">{{ money(totalGross) }}</span>
      </div>
      <div class="summary-card">
        <span class="summary-label">{{ data.factoryName ? '当前成衣厂' : '涉及成衣厂' }}</span>
        <span class="summary-value">{{ data.factoryName || `${filteredData.length} 家` }}</span>
      </div>
    </div>

    <!-- 明细表：只渲染当前页，汇总行仍按筛选后的整个区间算 -->
    <div v-if="filteredData.length && !totalBill && !totalSalary" class="empty-hint">
      这个条件下没有账单和工资记录（名称是按包含关系匹配的，日期不选时默认查本月 1 号到今天）。
    </div>
    <div class="table-wrap">
      <el-table
        :data="pageData"
        style="width: 100%"
        height="100%"
        stripe
        fit
        highlight-current-row
        empty-text="暂无数据"
        show-summary
        :summary-method="getSummaries"
      >
        <el-table-column prop="factoryName" label="成衣厂" align="center" min-width="140" show-overflow-tooltip />
        <el-table-column prop="billTotal" label="账单收入" align="center" min-width="120">
          <template #default="scope">{{ money(scope.row.billTotal) }}</template>
        </el-table-column>
        <el-table-column prop="salaryTotal" label="工资支出" align="center" min-width="120">
          <template #default="scope">{{ money(scope.row.salaryTotal) }}</template>
        </el-table-column>
        <el-table-column prop="grossProfit" label="毛利" align="center" min-width="120">
          <template #default="scope">
            <span :class="{ negative: Number(scope.row.grossProfit) < 0 }">{{ money(scope.row.grossProfit) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="毛利/收入" align="center" min-width="110">
          <template #default="scope">{{ marginRate(scope.row) }}</template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页：和"成衣厂账单"页同一个样式 -->
    <div class="grossProfit-page">
      <el-pagination
        background
        :total="filteredData.length"
        v-model:current-page="data.currentPage"
        v-model:page-size="data.pageSize"
        :page-sizes="[5, 10, 20, 50, 100]"
        layout="sizes, prev, pager, next, jumper, ->"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
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
    .filter((row) => row.factoryName)
    .map((row) => ({ factoryName: row.factoryName }))
    .sort((a, b) => String(a.factoryName).localeCompare(String(b.factoryName), 'zh-Hans-CN'))
)

/** 选定某家厂就只看这一行；没选就全看。名称在库里唯一（factory_name 唯一键），可以按名字匹配。 */
const filteredData = computed(() => {
  if (!data.factoryName) return data.tableData
  return data.tableData.filter((row) => row.factoryName === data.factoryName)
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
 * 金额统一两位小数展示。后端算的是 BigDecimal，这里只负责显示。
 */
function money(value) {
  const number = Number(value || 0)
  return `¥${number.toFixed(2)}`
}

/**
 * 毛利率：没有收入时显示"–"，避免除零出现 Infinity。
 */
function marginRate(row) {
  const bill = Number(row.billTotal || 0)
  if (!bill) return '–'
  return `${((Number(row.grossProfit || 0) / bill) * 100).toFixed(1)}%`
}

/**
 * 表格底部合计行：口径是整个筛选区间，不是当前页，所以不用 pageData 求和。
 */
const getSummaries = ({ columns }) => {
  return columns.map((column, index) => {
    if (index === 0) return '总计（整个区间）'
    if (column.property === 'billTotal') return money(totalBill.value)
    if (column.property === 'salaryTotal') return money(totalSalary.value)
    if (column.property === 'grossProfit') return money(totalGross.value)
    if (index === columns.length - 1 && totalBill.value) {
      return `${((totalGross.value / totalBill.value) * 100).toFixed(1)}%`
    }
    return ''
  })
}
</script>

<style scoped>
.grossProfit-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  /* 和 FactoryBill.vue 等列表页一致：表格吃掉剩余高度，分页永远贴底，不随行数上下跳 */
  justify-content: space-between;
  gap: 10px;
  overflow: hidden;
}

/* 跟 FactoryBill.vue 的 .factoryBill-button 一套规则：不换行、固定宽度、超宽横向滚动 */
.grossProfit-toolbar {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  padding: 10px 12px;
  gap: 8px;
  border-bottom: 1px solid var(--el-border-color);
  overflow-x: auto;
  overflow-y: hidden;
}

.grossProfit-toolbar > * {
  flex-shrink: 0;
}

.grossProfit-toolbar .name-select {
  width: 180px;
}

.grossProfit-toolbar .date-picker {
  width: 145px;
}

.grossProfit-toolbar .reset-btn {
  width: 80px;
}

.grossProfit-toolbar :deep(.el-input__wrapper),
.grossProfit-toolbar :deep(.el-date-editor.el-input__wrapper) {
  min-height: 32px;
}

/* 提示文字允许被压缩，不参与固定宽度，避免把前面的控件挤变形 */
.toolbar-tip {
  flex: 1 1 auto;
  min-width: 0;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.summary-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 10px 12px 0;
}

.summary-card {
  flex: 1 1 160px;
  min-width: 150px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 14px;
  border-radius: 8px;
  background: var(--el-fill-color-lighter);
}

.summary-label {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.summary-value {
  font-size: 18px;
  font-weight: 600;
}

.negative {
  color: var(--el-color-danger);
}

.empty-hint {
  margin: 0 12px;
  padding: 8px 12px;
  border-radius: 6px;
  background: var(--el-color-warning-light-9);
  color: var(--el-color-warning-dark-2);
  font-size: 13px;
}

/* 表格吃掉剩余高度；内部自己滚，行数再多也不会把分页顶下去。
   flex-basis 必须是 0，否则 100 行那页会按内容高把容器撑破、分页被裁掉。 */
.table-wrap {
  flex: 1 1 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.grossProfit-page {
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  padding: 8px 0 16px;
}
</style>
