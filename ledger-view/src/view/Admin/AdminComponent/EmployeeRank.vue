<template>
  <div class="admin-page">
    <AdminPageHeader
      title="员工产能排行"
      desc="按员工统计这段时间做了多少件、拿了多少工资、出了几天勤，默认看最近 30 天。"
      :count="data.rows.length"
      count-label="人"
    />

    <div class="filter-card">
      <div class="filter-bar">
        <el-select
          v-model="data.factoryId"
          placeholder="全部成衣厂"
          clearable
          filterable
          class="filter-wide"
          @change="handleQuery"
          @clear="handleQuery"
        >
          <el-option
            v-for="item in data.factoryOptions"
            :key="item.id"
            :label="item.factoryName"
            :value="item.id"
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
            @change="handleQuery"
          />
          <span class="range-sep">→</span>
          <el-date-picker
            v-model="data.endDate"
            type="date"
            format="YYYY/MM/DD"
            value-format="YYYY-MM-DD"
            placeholder="结束日期"
            class="filter-date"
            @change="handleQuery"
          />
        </div>
        <div class="filter-actions">
          <span class="filter-hint">默认看最近 30 天</span>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </div>
      </div>
    </div>

    <div class="stat-grid">
      <div class="stat-card">
        <span class="stat-label">参与人数</span>
        <span class="stat-value">{{ data.rows.length }} 人</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">总件数</span>
        <span class="stat-value">{{ formatNumber(totalQuantity) }} 件</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">人均件数</span>
        <span class="stat-value">{{ formatNumber(averageQuantity) }} 件</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">工资合计</span>
        <span class="stat-value">{{ formatMoney(totalSalary) }}</span>
      </div>
    </div>

    <div class="table-card is-fill">
      <el-table
        :data="pageData"
        height="100%"
        stripe
        empty-text="这段时间还没有工作记录"
      >
        <el-table-column label="排名" width="86" align="center">
          <template #default="scope">
            <span class="rank-badge" :class="rankClass(scope.$index + (data.currentPage - 1) * data.pageSize)">
              {{ scope.$index + (data.currentPage - 1) * data.pageSize + 1 }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="employeeName" label="员工" min-width="180" align="center">
          <template #default="scope">
            <div class="entity-cell">
              <span class="entity-avatar">{{ (scope.row.employeeName || '?').slice(0, 1) }}</span>
              <span class="entity-name">{{ scope.row.employeeName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="件数" min-width="260" align="center">
          <template #default="scope">
            <div class="qty-cell">
              <span class="num qty-value">{{ formatNumber(scope.row.quantity) }}</span>
              <span class="qty-bar"><i :style="{ width: barWidth(scope.row.quantity) }"></i></span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="工资" min-width="150" align="center">
          <template #default="scope">
            <span class="money">{{ formatMoney(scope.row.salary) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="平均单价（元/件）" min-width="150" align="center">
          <template #default="scope">
            <span class="num">{{ priceText(scope.row.avgPrice) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="出勤" min-width="110" align="center">
          <template #default="scope">
            <span class="num">{{ scope.row.days }} 天</span>
          </template>
        </el-table-column>
        <el-table-column label="工序" min-width="200" align="center">
          <template #default="scope">
            <div class="mode-cell">
              <span v-if="scope.row.embossQty" class="mode-tag">压花 {{ formatNumber(scope.row.embossQty) }}</span>
              <span v-if="scope.row.glueQty" class="mode-tag is-glue">刮胶 {{ formatNumber(scope.row.glueQty) }}</span>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pager-row">
      <el-pagination
        background
        :total="data.rows.length"
        v-model:current-page="data.currentPage"
        v-model:page-size="data.pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import AdminPageHeader from '../AdminPageHeader.vue'
import { formatMoney, formatNumber } from '../adminShared.js'
import { recentDateRange } from '../../../base/dateRange.js'
import { queryAdminFactoryList } from '../../../network/admin/index.js'
import { queryEmployeeRank } from '../../../network/admin/rank.js'

const defaultRange = recentDateRange(30)

const data = reactive({
  factoryId: '',
  factoryOptions: [],
  startDate: defaultRange.startDate,
  endDate: defaultRange.endDate,
  rows: [],
  currentPage: 1,
  pageSize: 10
})

const totalQuantity = computed(() =>
  data.rows.reduce((sum, row) => sum + Number(row.quantity || 0), 0)
)

const totalSalary = computed(() =>
  data.rows.reduce((sum, row) => sum + Number(row.salary || 0), 0)
)

const averageQuantity = computed(() => {
  if (!data.rows.length) return 0
  return Math.round(totalQuantity.value / data.rows.length)
})

/** 当前页要显示的行；排行数据量不大，前端分页够了 */
const pageData = computed(() => {
  const start = (data.currentPage - 1) * data.pageSize
  return data.rows.slice(start, start + data.pageSize)
})

/** 第一名的件数作为柱子的满格，其他人的柱子按比例 */
const maxQuantity = computed(() =>
  data.rows.reduce((max, row) => Math.max(max, Number(row.quantity || 0)), 0)
)

onMounted(() => {
  loadFactoryOptions()
  fetchRank()
})

async function loadFactoryOptions() {
  const { data: res } = await queryAdminFactoryList('', 1, 500, 0)
  if (res.status !== 200) return
  const payload = res.data || {}
  data.factoryOptions = payload.records || payload || []
}

async function fetchRank() {
  const { data: res } = await queryEmployeeRank(
    data.factoryId || undefined,
    data.startDate || undefined,
    data.endDate || undefined
  )
  if (res.status !== 200) {
    ElMessage.error(res.message)
    data.rows = []
    return
  }
  data.rows = res.data?.rows || []
  clampCurrentPage()
}

function handleQuery() {
  data.currentPage = 1
  fetchRank()
}

function resetFilter() {
  data.factoryId = ''
  data.startDate = defaultRange.startDate
  data.endDate = defaultRange.endDate
  handleQuery()
}

function clampCurrentPage() {
  const maxPage = Math.max(1, Math.ceil(data.rows.length / data.pageSize))
  if (data.currentPage > maxPage) data.currentPage = maxPage
}

/** 前三名给金银铜，其余用普通灰底 */
function rankClass(index) {
  if (index === 0) return 'is-gold'
  if (index === 1) return 'is-silver'
  if (index === 2) return 'is-bronze'
  return ''
}

/** 单价普遍只有几分钱，两位小数会全变成 0.04，这里小于 1 元时保留 3 位 */
function priceText(price) {
  if (price == null) return '–'
  const number = Number(price)
  if (number < 1) return `¥${number.toFixed(3)}`
  return formatMoney(number)
}

function barWidth(quantity) {
  if (!maxQuantity.value) return '0%'
  return `${Math.max(4, (Number(quantity || 0) / maxQuantity.value) * 100)}%`
}
</script>

<style scoped>
.filter-hint {
  font-size: 12px;
  color: var(--admin-text-muted);
  margin-right: 4px;
}

.range-sep {
  color: var(--admin-text-muted);
  font-size: 13px;
}

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

.table-card.is-fill {
  flex: 1 1 0;
  min-height: 320px;
  display: flex;
  flex-direction: column;
}

.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: #f1f4fa;
  color: #5b6b86;
  font-size: 12.5px;
  font-weight: 600;
}

.rank-badge.is-gold {
  background: linear-gradient(135deg, #ffd76e, #f0a92a);
  color: #6b4400;
}

.rank-badge.is-silver {
  background: linear-gradient(135deg, #e6edf7, #c3cfe0);
  color: #47546b;
}

.rank-badge.is-bronze {
  background: linear-gradient(135deg, #f3c9a4, #d9905a);
  color: #5a2f0d;
}

.qty-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: center;
}

.qty-value {
  min-width: 56px;
  text-align: right;
  font-weight: 600;
  color: #1f2a44;
}

.qty-bar {
  flex: 1 1 auto;
  max-width: 130px;
  height: 6px;
  border-radius: 999px;
  background: #eef2f9;
  overflow: hidden;
}

.qty-bar i {
  display: block;
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #6f9dff, #3f7cff);
}

.mode-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  justify-content: center;
  flex-wrap: wrap;
}

.mode-tag {
  padding: 1px 8px;
  border-radius: 999px;
  font-size: 12px;
  color: #2f6bff;
  background: #eaf1ff;
}

.mode-tag.is-glue {
  color: #b7791f;
  background: #fdf3e2;
}
</style>
