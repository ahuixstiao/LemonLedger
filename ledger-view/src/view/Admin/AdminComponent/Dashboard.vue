<template>
  <div class="admin-page">
    <AdminPageHeader
      title="数据看板"
      desc="账单、工资、毛利和对账差额放在一屏，进来先看这四个数。"
      :count="data.daily.length"
      count-label="天"
    >
      <template #actions>
        <el-button @click="goReconcile">去对账</el-button>
        <el-button type="primary" :loading="data.loading" @click="fetchSummary">刷新</el-button>
      </template>
    </AdminPageHeader>

    <section class="filter-card">
      <div class="filter-bar">
        <div class="filter-dates">
          <el-date-picker
            v-model="data.startDate"
            type="date"
            placeholder="开始日期"
            value-format="YYYY-MM-DD"
            class="filter-date"
            @change="fetchSummary"
          />
          <span class="range-sep">→</span>
          <el-date-picker
            v-model="data.endDate"
            type="date"
            placeholder="结束日期"
            value-format="YYYY-MM-DD"
            class="filter-date"
            @change="fetchSummary"
          />
        </div>
        <span class="range-hint">默认看最近 30 天</span>
        <div class="filter-actions">
          <el-button type="primary" @click="fetchSummary">查询</el-button>
          <el-button @click="resetRange">重置</el-button>
        </div>
      </div>
    </section>

    <section v-loading="data.loading" class="stat-grid">
      <article class="stat-card">
        <span class="stat-label">账单金额</span>
        <strong class="stat-value">{{ formatMoney(data.summary.billAmount) }}</strong>
        <span class="stat-sub">{{ formatNumber(data.summary.billQty) }} 件</span>
      </article>
      <article class="stat-card">
        <span class="stat-label">工资支出</span>
        <strong class="stat-value">{{ formatMoney(data.summary.salaryTotal) }}</strong>
        <span class="stat-sub">{{ formatNumber(data.summary.claimedQty) }} 件已认领</span>
      </article>
      <article class="stat-card">
        <span class="stat-label">毛利</span>
        <strong class="stat-value" :class="{ 'is-negative': Number(data.summary.grossProfit) < 0 }">
          {{ formatMoney(data.summary.grossProfit) }}
        </strong>
        <span class="stat-sub">账单金额 - 工资支出</span>
      </article>
      <article class="stat-card">
        <span class="stat-label">毛利率</span>
        <strong class="stat-value" :class="{ 'is-negative': Number(data.summary.grossProfit) < 0 }">
          {{ data.summary.marginRate == null ? '–' : data.summary.marginRate + '%' }}
        </strong>
        <span class="stat-sub">{{ data.summary.diffBedCount }} 张床对不上</span>
      </article>
    </section>

    <section class="table-card">
      <div class="panel-head">
        <h3>每天做了多少</h3>
        <span class="panel-sub">柱高按账单件数，鼠标悬停看金额与工资</span>
      </div>
      <div class="bar-chart">
        <div v-for="item in data.daily" :key="item.date" class="bar-col" :title="`${item.date}｜账单 ${formatNumber(item.billQty)} 件 / ${formatMoney(item.billAmount)}｜工资 ${formatMoney(item.salary)}`">
          <span v-if="Number(item.billQty) > 0" class="bar-value">{{ formatNumber(item.billQty) }}</span>
          <div class="bar-track">
            <div class="bar-fill" :style="{ height: barHeight(item.billQty) }"></div>
          </div>
          <span class="bar-label">{{ shortDate(item.date) }}</span>
        </div>
      </div>
    </section>

    <section class="table-card">
      <div class="panel-head">
        <h3>最该处理的 5 张床</h3>
        <span class="panel-sub">差额 = 账单数量 - 实际完成数量，按绝对差额排序</span>
      </div>
      <el-table :data="data.summary.topDiff" class="front-table" empty-text="这段时间对得上，没有差异">
        <el-table-column prop="factoryName" label="成衣厂" min-width="160" align="center">
          <template #default="{ row }">
            <div class="entity-cell">
              <span class="entity-avatar">{{ (row.factoryName || '?').slice(0, 1) }}</span>
              <span>{{ row.factoryName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="number" label="床号" min-width="100" align="center" />
        <el-table-column prop="styleNumber" label="款式编号" min-width="120" align="center" />
        <el-table-column label="账单数量" min-width="100" align="center">
          <template #default="{ row }"><span class="num">{{ formatNumber(row.billQty) }}</span></template>
        </el-table-column>
        <el-table-column label="实际完成" min-width="100" align="center">
          <template #default="{ row }"><span class="num">{{ formatNumber(row.doneQty) }}</span></template>
        </el-table-column>
        <el-table-column label="差额" min-width="100" align="center">
          <template #default="{ row }">
            <span class="num" :class="Number(row.diffQty) < 0 ? 'is-over' : 'is-pending'">
              {{ Number(row.diffQty) > 0 ? '+' : '' }}{{ formatNumber(row.diffQty) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="110" align="center">
          <template #default="{ row }">
            <span v-if="Number(row.diffQty) < 0" class="status-pill is-removed">做超了</span>
            <span v-else-if="Number(row.doneQty) === 0" class="status-pill is-warning">未开工</span>
            <span v-else class="status-pill is-warning">未做完</span>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AdminPageHeader from '../AdminPageHeader.vue'
import { formatMoney, formatNumber } from '../adminShared.js'
import { queryDashboardSummary } from '../../../network/admin/index.js'
import { recentDateRange } from '../../../base/dateRange.js'

const router = useRouter()

const defaultRange = recentDateRange(30)

const data = reactive({
  loading: false,
  startDate: defaultRange.startDate,
  endDate: defaultRange.endDate,
  daily: [],
  summary: {
    billQty: 0,
    billAmount: 0,
    salaryTotal: 0,
    grossProfit: 0,
    marginRate: null,
    claimedQty: 0,
    totalBedCount: 0,
    diffBedCount: 0,
    topDiff: []
  }
})

const maxQty = computed(() =>
  Math.max(1, ...data.daily.map(item => Number(item.billQty) || 0))
)

const barHeight = qty => {
  const ratio = (Number(qty) || 0) / maxQty.value
  return `${Math.max(4, Math.round(ratio * 100))}%`
}

const shortDate = date => (date || '').slice(5).replace('-', '/')

const fetchSummary = async () => {
  if (data.startDate && data.endDate && data.startDate > data.endDate) {
    ElMessage.error('开始日期不能晚于结束日期')
    return
  }
  data.loading = true
  try {
    const { data: res } = await queryDashboardSummary(data.startDate, data.endDate)
    if (res.status !== 200) {
      ElMessage.error(res.message || '看板加载失败')
      return
    }
    const payload = res.data || {}
    data.daily = payload.daily || []
    data.summary = {
      billQty: payload.billQty || 0,
      billAmount: payload.billAmount || 0,
      salaryTotal: payload.salaryTotal || 0,
      grossProfit: payload.grossProfit || 0,
      marginRate: payload.marginRate,
      claimedQty: payload.claimedQty || 0,
      totalBedCount: payload.totalBedCount || 0,
      diffBedCount: payload.diffBedCount || 0,
      topDiff: payload.topDiff || []
    }
  } catch (error) {
    ElMessage.error('看板加载失败，请稍后重试')
  } finally {
    data.loading = false
  }
}

const resetRange = () => {
  const range = recentDateRange(30)
  data.startDate = range.startDate
  data.endDate = range.endDate
  fetchSummary()
}

const goReconcile = () => router.push('/admin/reconcile')

onMounted(fetchSummary)
</script>

<style scoped>

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
  gap: 14px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px 16px;
  background: #fff;
  border: 1px solid #e6edf9;
  border-radius: 12px;
}

.stat-label {
  font-size: 12.5px;
  color: var(--admin-text-secondary);
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  font-variant-numeric: tabular-nums;
}

.stat-sub {
  font-size: 12px;
  color: var(--admin-text-muted);
}
.range-hint {
  font-size: 12.5px;
  color: var(--admin-text-muted);
}

.panel-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 18px 8px;
}

.panel-head h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.panel-sub {
  font-size: 12.5px;
  color: var(--admin-text-muted);
}

.bar-chart {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  padding: 10px 18px 18px;
  min-height: 200px;
}

.bar-col {
  flex: 1 1 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.bar-value {
  font-size: 12px;
  color: var(--admin-text-secondary);
  font-variant-numeric: tabular-nums;
}

.bar-track {
  width: 100%;
  height: 120px;
  display: flex;
  align-items: flex-end;
  background: #f4f7fd;
  border-radius: 8px;
  overflow: hidden;
}

.bar-fill {
  width: 100%;
  background: linear-gradient(180deg, #7aa7ff, var(--brand-1));
  border-radius: 8px 8px 0 0;
  transition: height 0.3s ease;
}

.bar-label {
  font-size: 12px;
  color: var(--admin-text-muted);
}

.table-card + .table-card {
  margin-top: 16px;
}

.stat-value.is-negative {
  color: #e0382a;
}
</style>
