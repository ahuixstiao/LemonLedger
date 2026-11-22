<template>
  <div class="admin-page">
    <AdminPageHeader
      title="账单结算"
      desc="每次导出成衣厂账单都会自动留一份快照；在这里登记已付金额，就知道哪一批结清了、还欠多少。"
      :count="data.total"
      count-label="张结算单">
      <template #actions>
        <el-button @click="goExport">去导出账单</el-button>
        <el-button type="primary" :loading="data.loading" @click="fetchList">刷新</el-button>
      </template>
    </AdminPageHeader>

    <div class="filter-card">
      <div class="filter-bar">
        <el-select v-model="data.factoryId" placeholder="全部成衣厂" clearable class="filter-wide" @change="handleSearch">
          <el-option v-for="item in data.factoryOptions" :key="item.id" :label="item.factoryName" :value="item.id" />
        </el-select>
        <el-select v-model="data.status" placeholder="全部状态" clearable @change="handleSearch">
          <el-option label="未结清" :value="0" />
          <el-option label="部分结清" :value="1" />
          <el-option label="已结清" :value="2" />
        </el-select>
        <div class="filter-dates">
          <el-date-picker
            v-model="data.startDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="开始日期"
            class="filter-date" />
          <span class="filter-dates-sep">→</span>
          <el-date-picker
            v-model="data.endDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="结束日期"
            class="filter-date" />
        </div>
        <div class="filter-actions">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </div>
    </div>

    <div class="table-card is-fill">
      <el-table v-loading="data.loading" :data="data.tableData" height="100%" border stripe>
        <el-table-column label="导出日期" width="96" align="center">
          <template #default="scope">
            <span class="date-cell">{{ formatDate(scope.row.createdDate) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="成衣厂" min-width="118" align="center">
          <template #default="scope">
            <div class="entity-cell">
              <span class="entity-avatar">{{ (scope.row.factoryName || '厂').slice(0, 1) }}</span>
              <span>{{ scope.row.factoryName || '–' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="账单区间" min-width="146" align="center">
          <template #default="scope">
            <span class="date-cell">{{ rangeText(scope.row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="笔数 / 件数" width="96" align="center">
          <template #default="scope">
            <div class="count-cell">
              <span>{{ formatNumber(scope.row.billCount) }} 笔</span>
              <span class="muted">{{ formatNumber(scope.row.totalQuantity) }} 件</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="账单金额" width="104" align="center">
          <template #default="scope">
            <span class="money">{{ formatMoney(scope.row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="已付" width="92" align="center">
          <template #default="scope">
            <span class="money">{{ formatMoney(scope.row.paidAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="还欠" width="92" align="center">
          <template #default="scope">
            <span class="money" :class="{ 'is-pending': unpaid(scope.row) > 0 }">{{ formatMoney(unpaid(scope.row)) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="118" align="center">
          <template #default="scope">
            <span class="status-pill" :class="statusClass(scope.row.status)">{{ statusText(scope.row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="208" align="center" fixed="right">
          <template #default="scope">
            <div class="action-links">
            <el-button link type="primary" @click="openItems(scope.row)">明细</el-button>
            <el-button link type="primary" @click="openSettle(scope.row)">登记结清</el-button>
            <el-button link type="primary" @click="downloadSnapshot(scope.row)">重导</el-button>
            <el-button link type="danger" @click="handleVoid(scope.row)">作废</el-button>
            </div>
          </template>
        </el-table-column>
        <template #empty>
          <span class="muted">还没有结算单，去「成衣厂账单」导出一次就会自动生成</span>
        </template>
      </el-table>

      <div class="pager-row front-pager">
        <el-pagination
          background
          :current-page="data.currentPage"
          :page-size="data.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="data.total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange" />
      </div>
    </div>

    <!-- 明细 -->
    <el-dialog v-model="data.itemsVisible" title="结算单明细" width="820px" class="app-dialog">
      <div v-if="data.currentSettlement" class="items-summary">
        <span>{{ data.currentSettlement.factoryName }}</span>
        <span>{{ rangeText(data.currentSettlement) }}</span>
        <span>{{ formatNumber(data.currentSettlement.billCount) }} 笔 · {{ formatNumber(data.currentSettlement.totalQuantity) }} 件</span>
        <span class="money">{{ formatMoney(data.currentSettlement.totalAmount) }}</span>
      </div>
      <el-table :data="data.items" max-height="420" border size="small">
        <el-table-column label="床号" prop="number" width="120" align="center" />
        <el-table-column label="款式编号" prop="styleNumber" width="130" align="center" />
        <el-table-column label="工作类型" prop="category" width="110" align="center" />
        <el-table-column label="数量" width="100" align="center">
          <template #default="scope">
            <span class="num">{{ formatNumber(scope.row.quantity) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="110" align="center">
          <template #default="scope">
            <span class="num">{{ priceText(scope.row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="金额" align="center">
          <template #default="scope">
            <span class="money">{{ formatMoney(scope.row.amount) }}</span>
          </template>
        </el-table-column>
        <template #empty>
          <span class="muted">这张结算单没有明细</span>
        </template>
      </el-table>
    </el-dialog>

    <!-- 登记结清 -->
    <el-dialog v-model="data.settleVisible" title="登记结清" width="440px" class="app-dialog">
      <el-form label-position="top">
        <el-form-item label="账单金额">
          <span class="money settle-amount">{{ formatMoney(data.settleForm.totalAmount) }}</span>
        </el-form-item>
        <el-form-item label="已付金额">
          <el-input v-model="data.settleForm.paidAmount" placeholder="填这次累计已付了多少">
            <template #prefix>¥</template>
          </el-input>
        </el-form-item>
        <el-form-item label="付款日期">
          <el-date-picker
            v-model="data.settleForm.paidDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择付款日期"
            style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="data.settleForm.remark" type="textarea" :rows="2" maxlength="120" show-word-limit placeholder="比如：微信转账、留了尾款" />
        </el-form-item>
        <div class="settle-hint muted">还差 {{ formatMoney(settleRemain()) }}；已付金额等于账单金额时自动标成已结清。</div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <span class="muted">Esc 关闭</span>
          <div>
            <el-button @click="data.settleVisible = false">取消</el-button>
            <el-button type="primary" :loading="data.saving" @click="submitSettle">保存</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import AdminPageHeader from '../AdminPageHeader.vue'
import { confirmDelete, formatMoney, formatNumber, formatDate } from '../adminShared.js'
import {
  exportSettlementExcel,
  querySettlementItems,
  querySettlementList,
  settleSettlement,
  voidSettlement
} from '../../../network/admin/settlement.js'
import { queryAdminFactoryList } from '../../../network/admin/factory.js'

const router = useRouter()

const data = reactive({
  loading: false,
  saving: false,
  factoryId: '',
  status: '',
  startDate: '',
  endDate: '',
  currentPage: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  factoryOptions: [],
  itemsVisible: false,
  items: [],
  currentSettlement: null,
  settleVisible: false,
  settleForm: {
    id: null,
    totalAmount: 0,
    paidAmount: '',
    paidDate: '',
    remark: ''
  }
})

function unpaid(row) {
  const total = Number(row.totalAmount) || 0
  const paid = Number(row.paidAmount) || 0
  return Math.max(total - paid, 0)
}

function shortDate(value) {
  if (!value) return ''
  const text = String(value)
  const year = String(new Date().getFullYear())
  return text.startsWith(year + '-') ? text.slice(5) : text
}

function rangeText(row) {
  if (!row) return '–'
  if (!row.startDate && !row.endDate) return '全部'
  const start = shortDate(row.startDate) || '起'
  const end = shortDate(row.endDate) || '至'
  return `${start} → ${end}`
}

function statusText(status) {
  if (Number(status) === 2) return '已结清'
  if (Number(status) === 1) return '部分结清'
  return '未结清'
}

function statusClass(status) {
  if (Number(status) === 2) return 'is-active'
  if (Number(status) === 1) return 'is-warning'
  return 'is-removed'
}

function priceText(price) {
  if (price == null) return '–'
  const number = Number(price)
  return number < 1 ? `¥${number.toFixed(3)}` : formatMoney(number)
}

function settleRemain() {
  const total = Number(data.settleForm.totalAmount) || 0
  const paid = Number(data.settleForm.paidAmount) || 0
  return Math.max(total - paid, 0)
}

async function loadFactoryOptions() {
  try {
    const { data: res } = await queryAdminFactoryList('', 1, 500, 0)
    if (res.status === 200) {
      data.factoryOptions = res.data.records || []
    }
  } catch (e) {
    data.factoryOptions = []
  }
}

async function fetchList() {
  data.loading = true
  try {
    const { data: res } = await querySettlementList({
      factoryId: data.factoryId,
      status: data.status,
      startDate: data.startDate,
      endDate: data.endDate,
      currentPage: data.currentPage,
      pageSize: data.pageSize
    })
    if (res.status === 200) {
      data.tableData = res.data.records || []
      data.total = res.data.total || 0
    } else {
      ElMessage.error(res.message || '查询结算单失败')
    }
  } catch (e) {
    ElMessage.error('查询结算单失败')
  } finally {
    data.loading = false
  }
}

function handleSearch() {
  data.currentPage = 1
  fetchList()
}

function handleReset() {
  data.factoryId = ''
  data.status = ''
  data.startDate = ''
  data.endDate = ''
  data.currentPage = 1
  fetchList()
}

function handlePageChange(page) {
  data.currentPage = page
  fetchList()
}

function handleSizeChange(size) {
  data.pageSize = size
  data.currentPage = 1
  fetchList()
}

function goExport() {
  router.push('/admin/factoryBillList')
}

async function openItems(row) {
  data.currentSettlement = row
  data.items = []
  data.itemsVisible = true
  try {
    const { data: res } = await querySettlementItems(row.id)
    if (res.status === 200) {
      data.items = res.data || []
    } else {
      ElMessage.error(res.message || '查询明细失败')
    }
  } catch (e) {
    ElMessage.error('查询明细失败')
  }
}

function openSettle(row) {
  data.settleForm = {
    id: row.id,
    totalAmount: Number(row.totalAmount) || 0,
    paidAmount: row.paidAmount == null ? '' : String(row.paidAmount),
    paidDate: row.paidDate || '',
    remark: row.remark || ''
  }
  data.settleVisible = true
}

async function submitSettle() {
  const paid = Number(data.settleForm.paidAmount)
  if (data.settleForm.paidAmount === '' || Number.isNaN(paid)) {
    ElMessage.warning('先填已付金额')
    return
  }
  data.saving = true
  try {
    const { data: res } = await settleSettlement(data.settleForm.id, {
      paidAmount: paid,
      paidDate: data.settleForm.paidDate,
      remark: data.settleForm.remark
    })
    if (res.status === 200) {
      ElMessage.success(paid >= data.settleForm.totalAmount ? '已标记为结清' : '已更新已付金额')
      data.settleVisible = false
      fetchList()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    data.saving = false
  }
}

async function downloadSnapshot(row) {
  try {
    const res = await exportSettlementExcel(row.id)
    const blob = res.data
    const disposition = res.headers?.['content-disposition'] || ''
    let fileName = `${row.factoryName || '成衣厂'}成衣厂账单-${row.id}.xlsx`
    const utf8Match = /filename\*=UTF-8''([^;\n]*)/i.exec(disposition)
    if (utf8Match) {
      fileName = decodeURIComponent(utf8Match[1])
    }
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = fileName
    link.click()
    URL.revokeObjectURL(link.href)
  } catch (e) {
    ElMessage.error('重新导出失败')
  }
}

async function handleVoid(row) {
  const ok = await confirmDelete(`确定作废「${row.factoryName || '该成衣厂'} ${rangeText(row)}」这张结算单吗？`)
  if (!ok) return
  try {
    const { data: res } = await voidSettlement(row.id)
    if (res.status === 200) {
      ElMessage.success('已作废')
      fetchList()
    } else {
      ElMessage.error(res.message || '作废失败')
    }
  } catch (e) {
    ElMessage.error('作废失败')
  }
}

onMounted(() => {
  loadFactoryOptions()
  fetchList()
})
</script>

<style scoped>
.items-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  align-items: center;
  margin-bottom: 12px;
  padding: 10px 14px;
  border-radius: 10px;
  background: #f7f9fd;
  font-size: 13px;
  color: var(--admin-text-secondary);
}

.settle-amount {
  font-size: 18px;
  font-weight: 600;
}

.settle-hint {
  font-size: 12.5px;
}

.action-links {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
}

.action-links :deep(.el-button) {
  margin-left: 0;
  padding: 2px 2px;
  font-size: 12.5px;
}

.count-cell {
  display: flex;
  flex-direction: column;
  line-height: 1.35;
  font-size: 12.5px;
}

.date-cell,
.items-summary {
  white-space: nowrap;
}

.dialog-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
</style>
