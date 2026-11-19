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
          <span class="filter-hint">默认看最近 30 天</span>
          <el-button type="primary" @click="handleFilterChange">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </div>
      </div>
    </div>


    <!-- 毛利趋势：按天折线，区间太长时自动按月聚合 -->
    <div class="trend-card">
      <div class="trend-head">
        <div>
          <span class="trend-title">毛利趋势</span>
          <span class="trend-sub">{{ trendGranularity === 'month' ? '按月聚合' : '按天' }} · {{ trendRangeText }}</span>
        </div>
        <div class="trend-legend">
          <span class="legend-item"><i class="dot dot-bill"></i>账单收入</span>
          <span class="legend-item"><i class="dot dot-salary"></i>工资支出</span>
          <span class="legend-item"><i class="dot dot-gross"></i>毛利</span>
        </div>
      </div>

      <div v-if="trendPoints.length" class="trend-body">
        <svg class="trend-svg" :viewBox="`0 0 ${chart.width} ${chart.height}`" preserveAspectRatio="none">
          <!-- 网格与 Y 轴刻度 -->
          <g v-for="tick in chart.ticks" :key="tick.value">
            <line :x1="chart.padLeft" :x2="chart.width - chart.padRight" :y1="tick.y" :y2="tick.y" class="grid-line" :class="{ 'is-zero': tick.value === 0 }" />
            <text :x="chart.padLeft - 8" :y="tick.y + 4" class="axis-text" text-anchor="end">{{ compactMoney(tick.value) }}</text>
          </g>

          <!-- 毛利面积 -->
          <path v-if="chart.grossArea" :d="chart.grossArea" class="gross-area" />
          <!-- 三条折线 -->
          <polyline :points="chart.billLine" class="line line-bill" />
          <polyline :points="chart.salaryLine" class="line line-salary" />
          <polyline :points="chart.grossLine" class="line line-gross" />

          <!-- 悬停竖线 + 点 -->
          <g v-if="hoverIndex !== null">
            <line :x1="chart.pointX(hoverIndex)" :x2="chart.pointX(hoverIndex)" :y1="chart.padTop" :y2="chart.height - chart.padBottom" class="hover-line" />
            <circle :cx="chart.pointX(hoverIndex)" :cy="chart.pointY(billValues[hoverIndex])" r="3.5" class="dot-mark dot-bill" />
            <circle :cx="chart.pointX(hoverIndex)" :cy="chart.pointY(salaryValues[hoverIndex])" r="3.5" class="dot-mark dot-salary" />
            <circle :cx="chart.pointX(hoverIndex)" :cy="chart.pointY(grossValues[hoverIndex])" r="3.5" class="dot-mark dot-gross" />
          </g>

          <!-- X 轴标签 -->
          <text
            v-for="label in chart.xLabels"
            :key="label.x"
            :x="label.x"
            :y="chart.height - 8"
            class="axis-text"
            text-anchor="middle"
          >{{ label.text }}</text>

          <!-- 透明热区，用来算悬停的是哪一天 -->
          <rect
            v-for="(point, index) in trendPoints"
            :key="point.key"
            :x="chart.pointX(index) - chart.stepX / 2"
            :y="chart.padTop"
            :width="chart.stepX"
            :height="chart.height - chart.padTop - chart.padBottom"
            class="hover-zone"
            @mouseenter="hoverIndex = index"
            @mouseleave="hoverIndex = null"
          />
        </svg>

        <div v-if="hoverIndex !== null" class="trend-tip" :style="chart.tipStyle(hoverIndex)">
          <div class="tip-date">{{ trendPoints[hoverIndex].label }}</div>
          <div class="tip-row"><span class="dot dot-bill"></span>账单收入<b>{{ moneyText(billValues[hoverIndex]) }}</b></div>
          <div class="tip-row"><span class="dot dot-salary"></span>工资支出<b>{{ moneyText(salaryValues[hoverIndex]) }}</b></div>
          <div class="tip-row"><span class="dot dot-gross"></span>毛利<b :class="{ 'is-negative': grossValues[hoverIndex] < 0 }">{{ moneyText(grossValues[hoverIndex]) }}</b></div>
        </div>
      </div>
      <div v-else class="trend-empty">这段时间还没有账单或工资记录</div>
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
        <el-table-column prop="factoryName" label="成衣厂" show-overflow-tooltip min-width="320" align="center">
          <template #default="scope">
            <div class="entity-cell">
              <span class="entity-avatar">{{ (scope.row.factoryName || '?').slice(0, 1) }}</span>
              <span class="entity-name">{{ scope.row.factoryName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="billTotal" label="账单收入" min-width="180" align="center">
          <template #default="scope">
            <span class="money">{{ formatMoney(scope.row.billTotal) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="salaryTotal" label="工资支出" min-width="180" align="center">
          <template #default="scope">
            <span class="money">{{ formatMoney(scope.row.salaryTotal) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="grossProfit" label="毛利" min-width="180" align="center">
          <template #default="scope">
            <span class="money" :class="{ 'is-negative': Number(scope.row.grossProfit) < 0 }">
              {{ formatMoney(scope.row.grossProfit) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="毛利率" min-width="140" align="center">
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import AdminPageHeader from '../AdminPageHeader.vue'
import { formatMoney } from '../adminShared.js'
import { recentDateRange } from '../../../base/dateRange.js'
import { queryFactoryGrossProfit, queryGrossTrend } from '../../../network/admin/index.js'

const defaultRange = recentDateRange(30)

const data = reactive({
  factoryName: '',
  startDate: defaultRange.startDate,
  endDate: defaultRange.endDate,
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


/* ---------------- 毛利趋势 ---------------- */

const trendData = reactive({
  points: [],
  totalBill: 0,
  totalSalary: 0,
  totalGross: 0,
  startDate: '',
  endDate: ''
})

const hoverIndex = ref(null)

/** 区间超过 45 天就按月聚合，免得每天一根点挤成一团 */
const trendGranularity = computed(() => (trendData.points.length > 45 ? 'month' : 'day'))

/** 按月聚合后的点；按天时就是原样 */
const trendPoints = computed(() => {
  const points = trendData.points
  if (trendGranularity.value === 'day') {
    return points.map(point => ({
      key: point.date,
      label: String(point.date).slice(5),
      bill: Number(point.billTotal || 0),
      salary: Number(point.salaryTotal || 0),
      gross: Number(point.grossProfit || 0)
    }))
  }
  const buckets = new Map()
  for (const point of points) {
    const month = String(point.date).slice(0, 7)
    const bucket = buckets.get(month) || { key: month, label: month, bill: 0, salary: 0, gross: 0 }
    bucket.bill += Number(point.billTotal || 0)
    bucket.salary += Number(point.salaryTotal || 0)
    bucket.gross += Number(point.grossProfit || 0)
    buckets.set(month, bucket)
  }
  return [...buckets.values()]
})

const billValues = computed(() => trendPoints.value.map(point => point.bill))
const salaryValues = computed(() => trendPoints.value.map(point => point.salary))
const grossValues = computed(() => trendPoints.value.map(point => point.gross))

const trendRangeText = computed(() => {
  if (!trendData.startDate || !trendData.endDate) return ''
  return `${trendData.startDate.slice(5)} 至 ${trendData.endDate.slice(5)}`
})

/** 图表的几何计算：留白、刻度、坐标、折线点串 */
const chart = computed(() => {
  const width = 980
  const height = 240
  const padLeft = 64
  const padRight = 18
  const padTop = 18
  const padBottom = 30
  const points = trendPoints.value
  const count = points.length

  const values = [...billValues.value, ...salaryValues.value, ...grossValues.value, 0]
  const rawMax = Math.max(...values)
  const rawMin = Math.min(...values)
  const span = rawMax - rawMin || 1

  // 把上下界扩成「整齐」的刻度（1/2/2.5/5 × 10^n），轴上是 2,000 这种整数而不是 1,822
  const roughStep = span / 4
  const magnitude = Math.pow(10, Math.floor(Math.log10(Math.abs(roughStep) || 1)))
  const candidates = [1, 2, 2.5, 5, 10].map(item => item * magnitude)
  const step = candidates.find(item => span / item <= 5) || candidates[candidates.length - 1]
  const yMin = Math.floor(rawMin / step) * step
  const yMax = Math.ceil(rawMax / step) * step

  const plotWidth = width - padLeft - padRight
  const plotHeight = height - padTop - padBottom
  const stepX = count > 1 ? plotWidth / (count - 1) : plotWidth
  const pointX = index => padLeft + (count > 1 ? stepX * index : plotWidth / 2)
  const pointY = value => {
    const ratio = (value - yMin) / (yMax - yMin || 1)
    return padTop + (1 - ratio) * plotHeight
  }

  const toLine = list => list.map((value, index) => `${pointX(index)},${pointY(value)}`).join(' ')

  const ticks = []
  for (let value = yMin; value <= yMax + step / 2; value += step) {
    ticks.push({ value, y: pointY(value) })
  }

  // X 轴：点太多就隔几个标一个
  const labelEvery = Math.max(1, Math.ceil(count / 8))
  const xLabels = points
    .map((point, index) => ({ x: pointX(index), text: point.label, index }))
    .filter(item => item.index % labelEvery === 0)
    .map(({ x, text }) => ({ x, text }))

  const grossArea = count > 1
    ? `M ${pointX(0)} ${pointY(0)} L ${points.map((_, index) => `${pointX(index)} ${pointY(grossValues.value[index])}`).join(' L ')} L ${pointX(count - 1)} ${pointY(0)} Z`
    : ''

  const tipStyle = index => {
    const x = pointX(index)
    const leftPercent = (x / width) * 100
    return {
      left: `${leftPercent}%`,
      transform: leftPercent > 70 ? 'translateX(-100%)' : 'translateX(-50%)'
    }
  }

  return {
    width,
    height,
    padLeft,
    padRight,
    padTop,
    padBottom,
    stepX,
    ticks,
    xLabels,
    billLine: toLine(billValues.value),
    salaryLine: toLine(salaryValues.value),
    grossLine: toLine(grossValues.value),
    grossArea,
    pointX,
    pointY,
    tipStyle
  }
})

/** Y 轴刻度用短数字（1.2万 / 3,400），避免一串小数把轴撑开 */
function compactMoney(value) {
  const number = Number(value || 0)
  if (Math.abs(number) >= 10000) return `${(number / 10000).toFixed(1)}万`
  return number.toLocaleString('zh-CN', { maximumFractionDigits: 0 })
}

/** 负数把钱符号放前面：-¥1,366.86，而不是 ¥-1,366.86 */
function moneyText(value) {
  const number = Number(value || 0)
  if (number < 0) return `-${formatMoney(Math.abs(number))}`
  return formatMoney(number)
}

async function fetchTrend() {
  const factory = filteredData.value.length === 1 ? filteredData.value[0] : null
  const { data: res } = await queryGrossTrend(
    factory ? factory.factoryId : undefined,
    data.startDate || undefined,
    data.endDate || undefined
  )
  if (res.status !== 200) {
    trendData.points = []
    return
  }
  const payload = res.data || {}
  trendData.points = payload.points || []
  trendData.totalBill = Number(payload.totalBill || 0)
  trendData.totalSalary = Number(payload.totalSalary || 0)
  trendData.totalGross = Number(payload.totalGross || 0)
  trendData.startDate = payload.startDate || ''
  trendData.endDate = payload.endDate || ''
  hoverIndex.value = null
}

onMounted(() => {
  fetchList()
  fetchTrend()
})

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
  fetchTrend()
}

function resetFilter() {
  data.factoryName = ''
  data.startDate = defaultRange.startDate
  data.endDate = defaultRange.endDate
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
.trend-card {
  background: #ffffff;
  border: 1px solid var(--admin-card-border);
  border-radius: 14px;
  box-shadow: 0 6px 20px rgba(30, 58, 112, 0.04);
  padding: 14px 16px 10px;
}

.trend-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 6px;
}

.trend-title {
  font-size: 13.5px;
  font-weight: 600;
  color: #1f2a44;
}

.trend-sub {
  margin-left: 8px;
  font-size: 12px;
  color: var(--admin-text-muted);
}

.trend-legend {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 12px;
  color: var(--admin-text-secondary);
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}

.dot-bill { background: #3f7cff; }
.dot-salary { background: #f5a524; }
.dot-gross { background: #22a06b; }

.trend-body {
  position: relative;
}

.trend-svg {
  width: 100%;
  height: 240px;
  display: block;
}

.grid-line {
  stroke: #eef2f9;
  stroke-width: 1;
}

.grid-line.is-zero {
  stroke: #d6dfee;
  stroke-dasharray: 4 3;
}

.axis-text {
  fill: #8a97ad;
  font-size: 11px;
}

.line {
  fill: none;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.line-bill { stroke: #3f7cff; }
.line-salary { stroke: #f5a524; stroke-dasharray: 5 4; }
.line-gross { stroke: #22a06b; stroke-width: 2.4; }

.gross-area {
  fill: rgba(34, 160, 107, 0.1);
  stroke: none;
}

.hover-line {
  stroke: #c3cfe4;
  stroke-dasharray: 3 3;
}

.dot-mark {
  stroke: #ffffff;
  stroke-width: 1.5;
}

.hover-zone {
  fill: transparent;
}

.trend-tip {
  position: absolute;
  top: 0;
  min-width: 150px;
  padding: 8px 10px;
  border-radius: 10px;
  background: rgba(31, 42, 68, 0.94);
  color: #ffffff;
  font-size: 12px;
  line-height: 1.7;
  pointer-events: none;
  box-shadow: 0 8px 22px rgba(16, 24, 40, 0.22);
}

.tip-date {
  font-weight: 600;
  margin-bottom: 2px;
}

.tip-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.tip-row b {
  margin-left: auto;
  font-variant-numeric: tabular-nums;
}

.tip-row b.is-negative {
  color: #ff9d92;
}

.trend-empty {
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
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
