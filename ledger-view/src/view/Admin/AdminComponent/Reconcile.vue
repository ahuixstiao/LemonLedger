<template>
  <div class="admin-page">
    <AdminPageHeader
      title="账单与认领对账"
      desc="成衣厂报的账单数量，和员工实际认领的数量放在同一行对比；差额 = 账单数量 - 实际完成数量，正数是还没做完，负数是做超了。"
      :count="filteredData.length"
      count-label="张床"
    />

    <div class="filter-card">
      <div class="filter-bar">
        <el-select
          v-model="data.factoryId"
          placeholder="全部成衣厂"
          clearable
          filterable
          class="filter-wide"
          @change="handleFilterChange"
          @clear="handleFilterChange"
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
        <el-checkbox v-model="data.onlyDiff" @change="handleFilterChange">只看有差异</el-checkbox>
        <div class="filter-actions">
          <el-button type="primary" @click="handleFilterChange">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </div>
      </div>
    </div>

    <!-- 汇总口径是「筛选后的整个区间」，不是当前这一页 -->
    <div class="stat-grid">
      <div class="stat-card">
        <span class="stat-label">账单数量</span>
        <span class="stat-value">{{ formatNumber(totalBillQty) }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">实际完成</span>
        <span class="stat-value">{{ formatNumber(totalDoneQty) }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">差额</span>
        <span class="stat-value" :class="{ 'is-negative': totalDiff < 0 }">{{ formatNumber(totalDiff) }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">有差异的床</span>
        <span class="stat-value" :class="{ 'is-negative': diffCount > 0 }">{{ formatNumber(diffCount) }}</span>
      </div>
    </div>

    <div class="table-card is-fill">
      <el-table
        :data="pageData"
        height="100%"
        stripe
        highlight-current-row
        empty-text="这个条件下没有账单，也没有认领记录"
      >
        <el-table-column prop="factoryName" label="成衣厂" min-width="158" align="center">
          <template #default="scope">
            <div class="entity-cell">
              <span class="entity-avatar">{{ (scope.row.factoryName || '?').slice(0, 1) }}</span>
              <span class="entity-name">{{ scope.row.factoryName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="number" label="床号" min-width="92" align="center" />
        <el-table-column prop="styleNumber" label="款式编号" min-width="96" align="center">
          <template #default="scope">
            <span>{{ styleText(scope.row.styleNumber) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="billQty" label="账单数量" min-width="88" align="center">
          <template #default="scope">
            <span class="num">{{ formatNumber(scope.row.billQty) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="doneQty" label="实际完成" min-width="88" align="center">
          <template #default="scope">
            <span class="num">{{ formatNumber(scope.row.doneQty) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="diffQty" label="差额" min-width="88" align="center">
          <template #default="scope">
            <span class="num" :class="diffClass(scope.row.diffQty)">{{ signNumber(scope.row.diffQty) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="billAmount" label="账单金额" min-width="110" align="center">
          <template #default="scope">
            <span class="money">{{ formatMoney(scope.row.billAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="salaryTotal" label="工资" min-width="110" align="center">
          <template #default="scope">
            <span class="money">{{ formatMoney(scope.row.salaryTotal) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="92" align="center">
          <template #default="scope">
            <span class="status-pill" :class="statusClass(scope.row.diffQty)">{{ statusText(scope.row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="146" align="center" show-overflow-tooltip>
          <template #default="scope">
            <span class="muted">{{ rowNote(scope.row) }}</span>
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
import { formatMoney, formatNumber } from '../adminShared.js'
import { queryBillReconcile, queryAdminFactoryList } from '../../../network/admin/index.js'

const data = reactive({
  factoryId: '',
  factoryOptions: [],
  startDate: '',
  endDate: '',
  onlyDiff: false,
  tableData: [],
  currentPage: 1,
  pageSize: 10
})

/** 只看有差异的床，免得几千行里翻；差额为 0 就是账平了 */
const filteredData = computed(() => {
  if (!data.onlyDiff) return data.tableData
  return data.tableData.filter(row => Number(row.diffQty || 0) !== 0)
})

const pageData = computed(() => {
  const start = (data.currentPage - 1) * data.pageSize
  return filteredData.value.slice(start, start + data.pageSize)
})

const totalBillQty = computed(() => sumField('billQty'))
const totalDoneQty = computed(() => sumField('doneQty'))
const totalDiff = computed(() => totalBillQty.value - totalDoneQty.value)
const diffCount = computed(() => filteredData.value.filter(row => Number(row.diffQty || 0) !== 0).length)

onMounted(async () => {
  await loadFactoryOptions()
  await fetchList()
})

async function loadFactoryOptions() {
  const { data: res } = await queryAdminFactoryList('', 1, 500, 0)
  if (res.status !== 200) return
  data.factoryOptions = (res.data.records || []).map(item => ({ id: item.id, factoryName: item.factoryName }))
}

async function fetchList() {
  const { data: res } = await queryBillReconcile(data.factoryId, data.startDate || undefined, data.endDate || undefined)
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
  data.factoryId = ''
  data.startDate = ''
  data.endDate = ''
  data.onlyDiff = false
  handleFilterChange()
}

function clampCurrentPage() {
  const maxPage = Math.max(1, Math.ceil(filteredData.value.length / data.pageSize))
  if (data.currentPage > maxPage) data.currentPage = maxPage
}

/** 款式编号为 0 或空时显示破折号，别让人以为真有 0 号款 */
function styleText(value) {
  const text = String(value ?? '').trim()
  return !text || text === '0' ? '–' : text
}

/** 差额带符号显示：正数前面加 +，0 显示 0，不改原始数值 */
function signNumber(value) {
  const num = Number(value || 0)
  if (num > 0) return `+${formatNumber(num)}`
  return formatNumber(num)
}

function diffClass(value) {
  const num = Number(value || 0)
  if (num > 0) return 'is-pending'
  if (num < 0) return 'is-over'
  return ''
}

function statusClass(value) {
  const num = Number(value || 0)
  if (num > 0) return 'is-warning'
  if (num < 0) return 'is-removed'
  return 'is-active'
}

function statusText(row) {
  const diff = Number(row.diffQty || 0)
  if (diff > 0) return Number(row.doneQty || 0) > 0 ? '未做完' : '未开工'
  if (diff < 0) return '做超了'
  return '已对平'
}

/** 一句话说明这行为什么有差：方便直接照着去查 */
function rowNote(row) {
  const diff = Number(row.diffQty || 0)
  if (!diff) {
    // 只做了一道工序（拿了一道工序的钱，但整床还没做完）也算账平，单独说明
    const salary = Number(row.salaryTotal || 0)
    return salary > 0 && !Number(row.billQty || 0) ? '只做了一道工序' : '已对平'
  }
  const bill = Number(row.billQty || 0)
  const done = Number(row.doneQty || 0)
  if (!bill) return `无账单 · 已做 ${formatNumber(done)}`
  if (!done) return '未认领'
  return diff > 0 ? `还差 ${formatNumber(diff)}` : `多做 ${formatNumber(-diff)}`
}
</script>

<style scoped>
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 16px;
  border: 1px solid var(--admin-border, #e6ecf5);
  border-radius: 12px;
  background: var(--card-bg, #fff);
}

.stat-label {
  font-size: 12.5px;
  color: var(--admin-text-secondary, #6b7a99);
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary, #1f2a44);
  font-variant-numeric: tabular-nums;
}

.stat-value.is-negative {
  color: #d92d20;
}

.num.is-pending {
  color: #d97706;
}

.num.is-over {
  color: #d92d20;
}

.status-pill.is-warning {
  background: #fef3e2;
  color: #d97706;
}
</style>
