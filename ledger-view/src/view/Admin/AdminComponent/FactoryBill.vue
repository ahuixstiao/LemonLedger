<template>
  <div class="admin-page">
    <AdminPageHeader
      title="成衣厂账单"
      desc="按成衣厂、床号、款式和日期查账。这里的数量与金额会直接进入毛利统计。"
      :count="data.total"
      count-label="条账单"
    >
      <template #stats>
        <span class="admin-count-chip" title="当前这一页的数量与金额合计">
          本页
          <strong>{{ formatNumber(pageQuantityTotal) }}</strong>
          <span>件</span>
          <strong>{{ formatMoney(pageBillTotal) }}</strong>
        </span>
      </template>
      <template #actions>
        <el-button @click="data.billVisible = true">
          <el-icon><TrendCharts /></el-icon>
          账单统计
        </el-button>
        <el-button type="primary" @click="openCreateBillDialog">
          <el-icon><Plus /></el-icon>
          添加账单
        </el-button>
      </template>
    </AdminPageHeader>

    <div class="filter-card">
      <div class="filter-bar">
        <el-select
          v-model="data.factoryId"
          filterable
          clearable
          placeholder="全部成衣厂"
          class="filter-wide"
          @change="onFilterChange"
        >
          <el-option v-for="item in data.factoryList" :key="item.id" :label="item.factoryName" :value="item.id" />
        </el-select>
        <el-input v-model="data.number" clearable placeholder="床号" @input="onSearch">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-input v-model="data.styleNumber" clearable placeholder="款式编号" @input="onSearch" />
        <el-select
          v-model="data.category"
          filterable
          clearable
          placeholder="工作类型"
          @change="onFilterChange"
        >
          <el-option
            v-for="item in data.jobCategoryList"
            :key="item.id"
            :label="item.category"
            :value="item.category"
          />
        </el-select>
        <el-select v-model="data.flag" placeholder="状态" @change="onFilterChange">
          <el-option label="正常" :value="0" />
          <el-option label="删除" :value="1" />
        </el-select>
        <div class="filter-dates">
          <el-date-picker
            v-model="data.startDate"
            type="date"
            format="YYYY/MM/DD"
            value-format="YYYY-MM-DD"
            placeholder="开始日期"
            class="filter-date"
            @change="onFilterChange"
          />
          <span class="range-sep">→</span>
          <el-date-picker
            v-model="data.endDate"
            type="date"
            format="YYYY/MM/DD"
            value-format="YYYY-MM-DD"
            placeholder="结束日期"
            class="filter-date"
            @change="onFilterChange"
          />
        </div>
        <div class="filter-actions">
          <el-button type="primary" @click="onFilterChange">查询</el-button>
          <el-button @click="resetQueryCondition">重置</el-button>
        </div>
      </div>
    </div>

    <div class="table-card">
      <el-table height="100%" :data="data.tableData" stripe highlight-current-row empty-text="没有找到账单记录">
        <el-table-column prop="factoryName" sortable label="厂名" min-width="180" align="center">
          <template #default="scope">
            <div class="entity-cell">
              <span class="entity-avatar">{{ (scope.row.factoryName || '?').slice(0, 1) }}</span>
              <span class="entity-name">{{ scope.row.factoryName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="number" sortable label="床号" min-width="90" align="center" />
        <el-table-column prop="styleNumber" sortable label="款式编号" show-overflow-tooltip min-width="140" align="center" />
        <el-table-column prop="category" sortable label="工作类型" min-width="120" align="center">
          <template #default="scope">
            <el-tag type="info" effect="plain">{{ scope.row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" sortable label="数量" min-width="90" align="center">
          <template #default="scope">
            <span class="num">{{ formatNumber(scope.row.quantity) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="bill" sortable label="账单" min-width="130" align="center">
          <template #default="scope">
            <span class="money">{{ formatMoney(scope.row.bill) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdDate" sortable label="日期" min-width="120" align="center">
          <template #default="scope">
            <span class="date-cell">{{ formatDate(scope.row.createdDate) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="flag" sortable label="状态" min-width="100" align="center">
          <template #default="scope">
            <span
              class="status-pill"
              :class="scope.row.flag === 0 ? 'is-active' : 'is-removed'"
            >{{ scope.row.flag === 0 ? '正常' : '删除' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="140" align="center">
          <template #default="scope">
            <span v-if="isReadOnlyRow(scope.row)" class="readonly-text">已删除，不可操作</span>
            <template v-else>
              <el-button text type="primary" @click="openEditBillDialog(scope.row)">编辑</el-button>
              <el-button text type="danger" @click="removeFactoryBill(scope.row)">删除</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pager-row">
      <el-pagination
        background
        :total="data.total"
        v-model:current-page="data.currentPage"
        v-model:page-size="data.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchFactoryBillList"
        @size-change="onSizeChange"
      />
    </div>

    <!-- 添加或编辑成衣厂账单 -->
    <el-dialog
      v-model="data.editBillDialogVisible"
      :title="data.editBillDialogMode === 0 ? '添加成衣厂账单' : '修改成衣厂账单'"
      width="520px"
    >
      <el-form
        ref="billInfoFormRef"
        :model="factoryBillInfoRef"
        :rules="addBillInfoRules"
        label-position="top"
      >
        <el-form-item label="成衣厂" prop="factoryId">
          <el-select
            filterable
            v-model="factoryBillInfoRef.factoryId"
            placeholder="选择成衣厂"
            @change="onBillFactoryChange"
          >
            <el-option v-for="item in data.factoryList" :key="item.id" :label="item.factoryName" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="床号" prop="number">
          <el-input v-model="factoryBillInfoRef.number" placeholder="输入床号" />
        </el-form-item>

        <el-form-item label="计件单价" prop="pieceRateId">
          <el-select
            filterable
            v-model="factoryBillInfoRef.pieceRateId"
            placeholder="选择款式 / 工作类型"
            @focus="queryFactoryStyleNumberListHandle(factoryBillInfoRef.factoryId)"
          >
            <el-option
              v-for="item in data.styleNumberList"
              :key="item.id"
              :label="`${item.styleNumber} / ${item.category}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="数量（件）" prop="quantity">
          <el-input v-model="factoryBillInfoRef.quantity" placeholder="输入数量" />
        </el-form-item>
        <el-form-item label="账单日期" prop="createdDate">
          <el-date-picker
            :editable="false"
            v-model="factoryBillInfoRef.createdDate"
            type="date"
            format="YYYY/MM/DD"
            value-format="YYYY-MM-DD"
            placeholder="账单日期"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeBillDialog">取消</el-button>
        <el-button type="primary" @click="validateBillForm(billInfoFormRef)">
          {{ data.editBillDialogMode === 0 ? '添加' : '保存' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 账单统计弹窗 -->
    <el-dialog v-model="data.billVisible" title="账单统计与导出" width="520px">
      <el-form
        ref="statisticalBillFormRef"
        label-position="top"
        :model="statisticalBillRef"
        :rules="statisticalBillRules"
      >
        <el-form-item label="成衣厂" prop="factoryId">
          <el-select filterable v-model="statisticalBillRef.factoryId" placeholder="请选择成衣厂">
            <el-option v-for="item in data.factoryList" :key="item.id" :label="item.factoryName" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="计算日期">
          <div class="date-range-row">
            <el-date-picker
              v-model="statisticalBillRef.startDate"
              type="date"
              format="YYYY/MM/DD"
              value-format="YYYY-MM-DD"
              placeholder="开始日期"
            />
            <span class="range-sep">→</span>
            <el-date-picker
              v-model="statisticalBillRef.endDate"
              type="date"
              format="YYYY/MM/DD"
              value-format="YYYY-MM-DD"
              placeholder="结束日期"
            />
          </div>
        </el-form-item>

        <el-form-item label="排序依据（可多选）" prop="sortFields">
          <el-select
            v-model="statisticalBillRef.sortFields"
            multiple
            clearable
            collapse-tags
            collapse-tags-tooltip
            placeholder="请选择一个或多个排序字段"
            @change="handleSortFieldsChange"
          >
            <el-option
              v-for="item in sortFieldOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item v-if="statisticalBillRef.sortFields.length" label="字段排序方向">
          <div class="sort-direction-list">
            <div v-for="field in statisticalBillRef.sortFields" :key="field" class="sort-direction-item">
              <span class="sort-direction-label">{{ getSortFieldLabel(field) }}</span>
              <el-select v-model="statisticalBillRef.sortOrderMap[field]" placeholder="选择排序方向">
                <el-option label="升序" value="asc" />
                <el-option label="降序" value="desc" />
              </el-select>
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="stat-submit" @click="statisticalBillForm(statisticalBillFormRef)">
            计算并导出账单
          </el-button>
        </el-form-item>

        <div class="stat-result">
          <span class="stat-result__label">账单总和</span>
          <span class="stat-result__value">¥{{ Number(outputValue || 0).toFixed(2) }}</span>
        </div>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, TrendCharts } from '@element-plus/icons-vue'
import { useTransition } from '@vueuse/core'
import AdminPageHeader from '../AdminPageHeader.vue'
import { confirmDelete, formatDate, formatMoney, formatNumber } from '../adminShared.js'
import { queryFactoryList } from '../../../network/index.js'
import {
  deleteFactoryBillInfo,
  editFactoryBillInfo,
  exportFactoryBillExcel,
  queryFactoryBillList,
  queryJobCategoryList,
  queryFactoryPieceRateStyleNumberList,
  saveFactoryBillInfo,
  statisticalFactoryBill
} from '../../../network/admin/index.js'
import {
  loadFactoryOptions,
  openCreateDialog,
  openEditDialog,
  resetReactiveForm,
  validateDialogForm
} from './factoryCrudShared.js'

const data = reactive({
  billVisible: false,
  tableData: [],
  factoryList: [],
  styleNumberList: [],
  factoryId: '',
  number: '',
  startDate: '',
  endDate: '',
  styleNumber: '',
  category: '',
  jobCategoryList: [],
  flag: 0,
  currentPage: 1,
  pageSize: 10,
  total: 0,
  editBillDialogVisible: false,
  editBillDialogMode: 0 // 0 为新增、1 为编辑
})

// 账单总数
const billTotal = ref(0)

// 数字滚动动画
const outputValue = useTransition(billTotal, {
  duration: 1000
})

onMounted(() => {
  loadFactoryList()
  loadJobCategoryList()
  fetchFactoryBillList()
})

const loadFactoryList = async () => {
  await loadFactoryOptions(data, queryFactoryList)
}

const loadJobCategoryList = async () => {
  const { data: res } = await queryJobCategoryList()
  if (res.status === 200) {
    data.jobCategoryList = Array.isArray(res.data) ? res.data : []
  } else {
    data.jobCategoryList = []
    ElMessage.error(res.message)
  }
}

// 查询成衣厂计件单价的款式编号列表
const queryFactoryStyleNumberListHandle = async factoryId => {
  if (!factoryId) {
    data.styleNumberList = []
    return
  }
  const { data: res } = await queryFactoryPieceRateStyleNumberList(factoryId)
  if (res.status === 200) {
    data.styleNumberList = res.data
  } else {
    data.styleNumberList = []
    ElMessage.error(res.message)
  }
}

const onBillFactoryChange = async factoryId => {
  factoryBillInfoRef.pieceRateId = ''
  await queryFactoryStyleNumberListHandle(factoryId)
}

// 查询成衣厂账单列表
const fetchFactoryBillList = async () => {
  const { data: res } = await queryFactoryBillList(
    data.factoryId,
    data.number,
    data.styleNumber,
    data.category,
    data.flag,
    data.startDate,
    data.endDate,
    data.currentPage,
    data.pageSize
  )
  if (res.status === 200) {
    data.tableData = res.data.records
    data.total = res.data.total
  } else {
    data.tableData = []
    data.total = 0
  }
}

const onSearch = () => {
  data.currentPage = 1
  fetchFactoryBillList()
}

const onFilterChange = () => {
  data.currentPage = 1
  fetchFactoryBillList()
}

const onSizeChange = () => {
  data.currentPage = 1
  fetchFactoryBillList()
}

// 统计并导出账单
const statisticalAndExportFactoryBillHandle = async () => {
  const { data: res } = await statisticalFactoryBill(
    statisticalBillRef.factoryId,
    statisticalBillRef.startDate,
    statisticalBillRef.endDate
  )
  if (res.status === 200) {
    billTotal.value = res.data.bill
    await exportFactoryBillExcelHandle()
  } else {
    billTotal.value = 0
    ElMessage.error(res.message)
  }
}

// 导出账单 Excel
const exportFactoryBillExcelHandle = async () => {
  const sortConfigList = (statisticalBillRef.sortFields || []).map(field => ({
    field,
    order: statisticalBillRef.sortOrderMap?.[field] || 'asc'
  }))

  const res = await exportFactoryBillExcel(
    statisticalBillRef.factoryId,
    statisticalBillRef.startDate,
    statisticalBillRef.endDate,
    sortConfigList
  )

  // 获取响应头中的文件名
  let fileName = '成衣厂账单.xlsx'
  const contentDisposition = res.headers?.['content-disposition'] || res.headers?.['Content-Disposition']
  if (contentDisposition) {
    const fileNameStarMatch = contentDisposition.match(/filename\*=UTF-8''([^;\n]*)/i)
    if (fileNameStarMatch && fileNameStarMatch[1]) {
      fileName = decodeURIComponent(fileNameStarMatch[1].replace(/"/g, ''))
    } else {
      const fileNameMatch = contentDisposition.match(/filename=([^;\n]*)/i)
      if (fileNameMatch && fileNameMatch[1]) {
        fileName = decodeURIComponent(fileNameMatch[1].replace(/['"]/g, ''))
      }
    }
  }

  const blob = new Blob([res.data], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  })

  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)

  ElMessage.success('下载成功')
}

/**
 * 账单被判重拦下来时，问一句「确实是返工/补做吗」，确认就带 confirmDuplicate 再存一次。
 * 以前这条规则是写死的 20 天且没有放行口子，返工的单子只能改日期绕过去。
 *
 * @param {string} message 后端返回的提示语
 * @returns {Promise<boolean>} 是否确认强存
 */
const confirmDuplicateAndRetry = async message => {
  if (!message || !message.includes('已存在相同账单')) return false
  try {
    await ElMessageBox.confirm(
      `${message}\n\n确认这一床确实是返工或补做、需要再记一次账吗？`,
      '可能是重复记账',
      { confirmButtonText: '确认，仍然保存', cancelButtonText: '取消', type: 'warning' }
    )
    return true
  } catch (cancelOrClose) {
    return false
  }
}

/**
 * 添加成功后弹窗不关，只把「床号 / 数量」清空，
 * 成衣厂、计件单价和账单日期留着，方便同一个厂同一天连着录好几床。
 */
const clearBillInputsAfterCreate = () => {
  factoryBillInfoRef.number = ''
  factoryBillInfoRef.quantity = ''
  billInfoFormRef.value?.clearValidate?.(['number', 'quantity'])
}

const createFactoryBill = async () => {
  const { data: res } = await saveFactoryBillInfo(factoryBillInfoRef)
  if (res.status === 200) {
    ElMessage.success(res.message)
    clearBillInputsAfterCreate()
    await fetchFactoryBillList()
    return
  }
  if (await confirmDuplicateAndRetry(res.message)) {
    const { data: retryRes } = await saveFactoryBillInfo(factoryBillInfoRef, true)
    if (retryRes.status === 200) {
      ElMessage.success(retryRes.message)
      clearBillInputsAfterCreate()
      await fetchFactoryBillList()
    } else {
      ElMessage.error(retryRes.message)
    }
    return
  }
  ElMessage.error(res.message)
}

const updateFactoryBill = async () => {
  const { data: res } = await editFactoryBillInfo(factoryBillInfoRef)
  if (res.status === 200) {
    ElMessage.success(res.message)
    await fetchFactoryBillList()
    data.editBillDialogVisible = false
    return
  }
  if (await confirmDuplicateAndRetry(res.message)) {
    const { data: retryRes } = await editFactoryBillInfo(factoryBillInfoRef, true)
    if (retryRes.status === 200) {
      ElMessage.success(retryRes.message)
      await fetchFactoryBillList()
      data.editBillDialogVisible = false
    } else {
      ElMessage.error(retryRes.message)
    }
    return
  }
  ElMessage.error(res.message)
  data.editBillDialogVisible = false
}

const removeFactoryBill = async row => {
  const confirmed = await confirmDelete(
    `确定删除「${row.factoryName} · 床号 ${row.number}」这条账单吗？删除后毛利统计会同步减少。`
  )
  if (!confirmed) return

  const { data: res } = await deleteFactoryBillInfo(row.id)
  if (res.status === 200) {
    ElMessage.success(res.message)
    await fetchFactoryBillList()
  } else {
    ElMessage.error(res.message)
  }
}

// 查询指定成衣厂账单表单提交校验函数
const statisticalBillForm = async formEl => {
  if (!formEl) return
  await formEl.validate(valid => {
    if (valid) {
      statisticalAndExportFactoryBillHandle()
    }
  })
}

// 本页合计：数量与金额，显示在页头，避免表格底部的合计行压住最后一行
const pageQuantityTotal = computed(() =>
  data.tableData.reduce((sum, row) => {
    const value = Number(row.quantity)
    return Number.isNaN(value) ? sum : sum + value
  }, 0)
)

const pageBillTotal = computed(() =>
  data.tableData.reduce((sum, row) => {
    const value = Number(row.bill)
    return Number.isNaN(value) ? sum : sum + value
  }, 0)
)

// 重置按钮点击事件
const resetQueryCondition = () => {
  data.factoryId = ''
  data.number = ''
  data.styleNumber = ''
  data.category = ''
  data.flag = 0
  data.startDate = ''
  data.endDate = ''
  data.currentPage = 1
  fetchFactoryBillList()
}

const statisticalBillFormRef = ref()
const billInfoFormRef = ref()

// 构建统计的账单实体
const statisticalBillRef = reactive({
  factoryId: '',
  startDate: '',
  endDate: '',
  sortFields: [],
  sortOrderMap: {}
})

// 成衣厂账单表单初始化数据
const factoryBillInfoInit = {
  id: '',
  factoryId: '',
  pieceRateId: '',
  number: '',
  quantity: '',
  createdDate: ''
}

const factoryBillInfoRef = reactive({ ...factoryBillInfoInit })

const openEditBillDialog = async bill => {
  await queryFactoryStyleNumberListHandle(bill.factoryId)
  openEditDialog(factoryBillInfoRef, bill, data, 'editBillDialogMode', 'editBillDialogVisible')
}

const openCreateBillDialog = () => {
  openCreateDialog(factoryBillInfoRef, factoryBillInfoInit, data, 'editBillDialogMode', 'editBillDialogVisible')
  data.styleNumberList = []
}

const resetBillForm = () => {
  resetReactiveForm(factoryBillInfoRef, factoryBillInfoInit, billInfoFormRef)
}

const closeBillDialog = () => {
  resetBillForm()
  data.editBillDialogVisible = false
}

const validateBillForm = async formEl => {
  await validateDialogForm(
    formEl,
    data.editBillDialogMode,
    createFactoryBill,
    updateFactoryBill,
    () => ElMessage.error('请检查是否填写正确')
  )
}

/**
 * 删除状态的数据仅允许展示，不提供编辑/删除入口。
 */
const isReadOnlyRow = row => {
  return Number(row?.flag) === 1 || Number(data.flag) === 1
}

const onlyNumberRule = {
  pattern: /^[0-9]+$/,
  message: '只能输入数字',
  trigger: 'blur'
}

// 添加账单记录校验规则
const addBillInfoRules = reactive({
  factoryId: [{ required: true, message: '请选择厂名', trigger: 'blur' }],
  pieceRateId: [{ required: true, message: '请选择计件单价', trigger: 'blur' }],
  number: [{ required: true, message: '请输入床号', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }, onlyNumberRule],
  createdDate: [{ required: true, message: '请选择账单日期', trigger: 'blur' }]
})

// 统计成衣厂账单校验规则
const statisticalBillRules = reactive({
  factoryId: [{ required: true, message: '请选择成衣厂', trigger: 'blur' }]
})

const sortFieldOptions = [
  { label: '床号', value: 'number' },
  { label: '创建日期', value: 'createdDate' },
  { label: '款式编号', value: 'styleNumber' },
  { label: '工作类型', value: 'category' },
  { label: '数量', value: 'quantity' },
  { label: '计件单价', value: 'price' },
  { label: '账单金额', value: 'bill' }
]

const getSortFieldLabel = field => {
  return sortFieldOptions.find(item => item.value === field)?.label || field
}

const handleSortFieldsChange = selectedFields => {
  const selectedSet = new Set(selectedFields || [])

  Object.keys(statisticalBillRef.sortOrderMap).forEach(field => {
    if (!selectedSet.has(field)) {
      delete statisticalBillRef.sortOrderMap[field]
    }
  })

  ;(selectedFields || []).forEach(field => {
    if (!statisticalBillRef.sortOrderMap[field]) {
      statisticalBillRef.sortOrderMap[field] = 'asc'
    }
  })
}
</script>

<style scoped>
.range-sep {
  color: var(--admin-text-muted);
  font-size: 13px;
}

.date-range-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.sort-direction-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.sort-direction-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.sort-direction-label {
  min-width: 88px;
  color: #475467;
  font-size: 13px;
}

.sort-direction-item :deep(.el-select) {
  width: 140px;
}

.stat-submit {
  width: 100%;
}

.stat-result {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 12px;
  background: #f4f8ff;
}

.stat-result__label {
  font-size: 13px;
  color: var(--admin-text-secondary);
}

.stat-result__value {
  font-size: 22px;
  font-weight: 700;
  color: var(--brand-1);
  font-variant-numeric: tabular-nums;
}
</style>
