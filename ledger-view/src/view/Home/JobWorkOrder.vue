<template>
  <div class="workOrder-container">
    <section class="page-header-card">
      <div>
        <h2 class="page-title">认领工单中心</h2>
        <p class="page-subtitle">快速筛选、认领与维护工单，提升分配效率</p>
      </div>
      <div class="page-stat">
        <span class="label">待处理工单</span>
        <span class="value">{{ data.total }}</span>
      </div>
    </section>

    <section class="panel-card">
      <CommonFilterBar
        v-model="workOrderFilters"
        :actions="workOrderFilterActions"
        :fields="workOrderFilterFields"
        :initial-value="workOrderFilterInitialValue"
        @action="onWorkOrderFilterAction"
        @query="fetchWorkOrderList"
        @reset="onWorkOrderFilterReset"
      />
    </section>

    <section class="workOrder-table-section panel-card table-card">
      <WorkOrderTable
        :table-data="data.tableData"
        :total="data.total"
        :current-page="data.currentPage"
        :page-size="data.pageSize"
        @update:currentPage="val => (data.currentPage = val)"
        @update:pageSize="val => (data.pageSize = val)"
        @query="fetchWorkOrderList"
        @claim="openClaimDialog"
        @edit="openEditWorkOrderDialog"
        @delete="removeWorkOrder"
      />
    </section>
    <!--  添加/编辑工单弹窗  -->
    <el-dialog
      v-model="data.editWorkOrderDialogVisible"
      :title="data.workOrderDialogMode === 0 ? '添加工单' : '编辑工单'"
      :width="data.isMobile ? '92%' : '50%'"
      :top="data.isMobile ? '4vh' : '12vh'"
      center
      class="responsive-dialog work-order-edit-dialog"
    >
      <el-form
        ref="workOrderFormRef"
        :model="workOrderFormModel"
        :rules="workOrderFormRules"
        label-width="auto"
        :label-position="data.isMobile ? 'top' : 'left'"
      >
        <el-form-item size="large" label="工厂名称:" prop="factoryId">
          <el-select filterable v-model="workOrderFormModel.factoryId" placeholder="选择工厂名称">
            <el-option v-for="item in data.factoryList" :key="item.id" :label="item.factoryName" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item size="large" label="床号" prop="number">
          <el-input v-model="workOrderFormModel.number" placeholder="输入床号" />
        </el-form-item>

        <el-form-item size="large" label="款式编号:" prop="styleNumber">
          <el-input v-model="workOrderFormModel.styleNumber" placeholder="输入款式编号" />
        </el-form-item>

        <el-form-item size="large" label="工作类型:" prop="categoryId">
          <el-select v-model="workOrderFormModel.categoryId" placeholder="选择工作类型">
            <el-option v-for="item in data.categoryList" :key="item.id" :label="item.category" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item size="large" label="数量:" prop="quantity">
          <el-input v-model="workOrderFormModel.quantity" placeholder="输入数量" />
        </el-form-item>

        <el-form-item size="large" label="工单日期:" prop="createdDate">
          <el-date-picker
            :editable="false"
            v-model="workOrderFormModel.createdDate"
            type="date"
            format="YYYY/MM/DD"
            value-format="YYYY-MM-DD"
            placeholder="工单日期"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeWorkOrderDialog">取消</el-button>
          <el-button type="primary" @click="validateWorkOrderForm(workOrderFormRef)">
            {{ data.workOrderDialogMode === 0 ? '添加' : '更新' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
    <!--  认领工单弹窗  -->
    <el-dialog
      v-model="data.claimDialogVisible"
      title="认领工单"
      :width="data.isMobile ? '92%' : '50%'"
      :top="data.isMobile ? '4vh' : '12vh'"
      center
      class="responsive-dialog claim-dialog"
    >
      <el-form
        ref="claimFormRef"
        :model="claimFormModel"
        :rules="claimFormRules"
        label-width="auto"
        :label-position="data.isMobile ? 'top' : 'left'"
      >
        <el-form-item size="large" label="员工:" prop="employeeId">
          <el-select v-model="claimFormModel.employeeId" placeholder="请选择员工姓名">
            <el-option v-for="item in data.employeeList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item size="large" label="厂名:">
          <el-input :model-value="claimFormModel.factoryName" disabled />
        </el-form-item>

        <el-form-item size="large" label="床号:">
          <el-input :model-value="claimFormModel.number" disabled />
        </el-form-item>

        <el-form-item size="large" label="款式编号:">
          <el-input :model-value="claimFormModel.styleNumber" disabled />
        </el-form-item>

        <el-form-item size="large" label="工作类型:">
          <el-input :model-value="claimFormModel.category" disabled />
        </el-form-item>

        <el-form-item size="large" label="数量:" prop="quantity">
          <el-input v-model="claimFormModel.quantity" placeholder="请输入数量" />
        </el-form-item>

        <el-form-item size="large" label="日期:">
          <el-input :model-value="claimFormModel.createdDate" disabled />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeClaimDialog">取消</el-button>
          <el-button type="primary" @click="submitClaimWorkOrder(claimFormRef)">提交</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, reactive, ref, computed, watch } from 'vue'
import { onBeforeRouteLeave, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import CommonFilterBar from './HomeComponent/CommonFilterBar.vue'
import WorkOrderTable from './HomeComponent/WorkOrderTable.vue'
import {
  queryEmployees,
  queryFactoryList,
  claimWorkOrder,
  deleteWorkOrder,
  queryWorkOrderList,
  saveWorkOrder,
  updateWorkOrder,
  queryCategoryList
} from '../../network/index.js'
import {
  loadFactoryOptions,
  openCreateDialog,
  resetReactiveForm,
  validateDialogForm
} from '../Admin/AdminComponent/factoryCrudShared.js'

const updateMobileState = () => {
  data.isMobile = window.matchMedia('(max-width: 768px)').matches
}

onMounted(() => {
  updateMobileState()
  window.addEventListener('resize', updateMobileState)
  loadFactoryList()
  loadCategoryList()
  loadEmployeeList()
  fetchWorkOrderList()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateMobileState)
})

const router = useRouter()

const data = reactive({
  tableData: [],
  factoryList: [],
  categoryList: [],
  employeeList: [],
  factoryId: '',
  number: '',
  startDate: '',
  endDate: '',
  styleNumber: '',
  categoryId: '',
  currentPage: 1,
  pageSize: 10,
  total: 0,
  editWorkOrderDialogVisible: false,
  workOrderDialogMode: 0,
  claimDialogVisible: false,
  isMobile: false
})

const workOrderFilterInitialValue = {
  factoryId: '',
  number: '',
  styleNumber: '',
  categoryId: '',
  startDate: '',
  endDate: ''
}

const workOrderFilters = computed({
  get: () => ({
    factoryId: data.factoryId,
    number: data.number,
    styleNumber: data.styleNumber,
    categoryId: data.categoryId,
    startDate: data.startDate,
    endDate: data.endDate
  }),
  set: value => {
    data.factoryId = value.factoryId ?? ''
    data.number = value.number ?? ''
    data.styleNumber = value.styleNumber ?? ''
    data.categoryId = value.categoryId ?? ''
    data.startDate = value.startDate ?? ''
    data.endDate = value.endDate ?? ''
  }
})

const workOrderFilterActions = [
  { key: 'create', label: '添加工单', type: 'primary' },
  { key: 'backHome', label: '返回首页' }
]

const workOrderFilterFields = computed(() => [
  {
    key: 'factoryId',
    type: 'select',
    placeholder: '选择工厂名称',
    filterable: true,
    clearable: true,
    options: data.factoryList,
    optionLabel: 'factoryName',
    optionValue: 'id'
  },
  {
    key: 'number',
    type: 'input',
    placeholder: '床号',
    clearable: true
  },
  {
    key: 'styleNumber',
    type: 'input',
    placeholder: '款式编号',
    clearable: true
  },
  {
    key: 'categoryId',
    type: 'select',
    placeholder: '选择工作类型',
    filterable: false,
    clearable: true,
    options: data.categoryList,
    optionLabel: 'category',
    optionValue: 'id'
  },
  {
    key: 'startDate',
    type: 'date',
    editable: false,
    placeholder: '开始日期',
    popperClass: 'month-only-picker'
  },
  {
    key: 'endDate',
    type: 'date',
    editable: false,
    placeholder: '结束日期',
    popperClass: 'month-only-picker'
  }
])

/**
 * 返回首页并清理认领员工缓存。
 */
const goHome = () => {
  clearClaimEmployeeCache()
  router.push('/')
}

const onWorkOrderFilterAction = action => {
  if (action === 'create') {
    openCreateWorkOrderDialog()
    return
  }
  if (action === 'backHome') {
    goHome()
  }
}

const loadFactoryList = async () => {
  await loadFactoryOptions(data, queryFactoryList)
}

const loadCategoryList = async () => {
  const {data: res} = await queryCategoryList()
  data.categoryList = res?.data || []
}

const loadEmployeeList = async () => {
  const { data: res } = await queryEmployees()
  data.employeeList = res?.data || []
}

const fetchWorkOrderList = async () => {
  const { data: res } = await queryWorkOrderList(
    data.factoryId,
    data.number,
    data.styleNumber,
    data.categoryId,
    data.startDate,
    data.endDate,
    data.currentPage,
    data.pageSize
  )

  if (res.status === 200) {
    data.tableData = res?.data?.records || []
    data.total = res?.data?.total || 0
  } else {
    data.tableData = []
    data.total = 0
    ElMessage.error(res.message)
  }
}

/**
 * 新增工单。
 */
const createWorkOrder = async () => {
  const payload = {
    factoryId: workOrderFormModel.factoryId,
    number: workOrderFormModel.number,
    styleNumber: workOrderFormModel.styleNumber,
    categoryId: workOrderFormModel.categoryId,
    quantity: Number(workOrderFormModel.quantity),
    createdDate: workOrderFormModel.createdDate
  }
  const { data: res } = await saveWorkOrder(payload)
  if (res.status === 200) {
    ElMessage.success(res.message)
    //closeWorkOrderDialog()
    await fetchWorkOrderList()
  } else {
    ElMessage.error(res.message)
  }
}

/**
 * 更新工单。
 */
const modifyWorkOrder = async () => {
  const payload = {
    id: workOrderFormModel.id,
    factoryId: workOrderFormModel.factoryId,
    number: workOrderFormModel.number,
    styleNumber: workOrderFormModel.styleNumber,
    categoryId: workOrderFormModel.categoryId,
    quantity: Number(workOrderFormModel.quantity),
    createdDate: workOrderFormModel.createdDate
  }
  const { data: res } = await updateWorkOrder(payload)
  if (res.status === 200) {
    ElMessage.success(res.message)
    closeWorkOrderDialog()
    await fetchWorkOrderList()
  } else {
    ElMessage.error(res.message)
  }
}

const removeWorkOrder = async id => {
  const { data: res } = await deleteWorkOrder(id)
  if (res.status === 200) {
    ElMessage.success(res.message)
    await fetchWorkOrderList()
  } else {
    ElMessage.error(res.message)
  }
}

const resetQueryCondition = () => {
  data.factoryId = ''
  data.number = ''
  data.styleNumber = ''
  data.categoryId = ''
  data.startDate = ''
  data.endDate = ''
  data.currentPage = 1
  fetchWorkOrderList()
}

const onWorkOrderFilterReset = () => {
  resetQueryCondition()
}

const workOrderFormRef = ref()
const workOrderFormInit = {
  id: '',
  factoryId: '',
  number: '',
  styleNumber: '',
  categoryId: '',
  quantity: '',
  createdDate: ''
}
const workOrderFormModel = reactive({ ...workOrderFormInit })

/**
 * 打开新增工单弹窗。
 */
const openCreateWorkOrderDialog = () => {
  openCreateDialog(
    workOrderFormModel,
    workOrderFormInit,
    data,
    'workOrderDialogMode',
    'editWorkOrderDialogVisible'
  )
}

/**
 * 打开编辑工单弹窗。
 */
const openEditWorkOrderDialog = workOrder => {
  Object.assign(workOrderFormModel, {
    id: workOrder.id,
    factoryId: workOrder.factoryId,
    number: workOrder.number,
    styleNumber: workOrder.styleNumber,
    categoryId: workOrder.categoryId,
    quantity: workOrder.quantity,
    createdDate: workOrder.createdDate
  })
  data.workOrderDialogMode = 1
  data.editWorkOrderDialogVisible = true
}

/**
 * 关闭工单弹窗并重置表单。
 */
const closeWorkOrderDialog = () => {
  resetReactiveForm(workOrderFormModel, workOrderFormInit, workOrderFormRef)
  data.workOrderDialogMode = 0
  data.editWorkOrderDialogVisible = false
}

const CLAIM_EMPLOYEE_CACHE_KEY = 'jobWorkOrder_selectedEmployeeId'

const claimFormRef = ref()
const claimFormInit = {
  employeeId: '',
  factoryId: '',
  factoryName: '',
  number: '',
  styleNumber: '',
  categoryId: '',
  category: '',
  quantity: '',
  createdDate: '',
  modeId: '',
  workOrderId: ''
}
const claimFormModel = reactive({ ...claimFormInit })

const resolveFactoryName = workOrder => {
  if (workOrder.factoryName) return workOrder.factoryName
  const factory = data.factoryList.find(item => item.id === workOrder.factoryId)
  return factory?.factoryName || ''
}

const resolveCategoryName = workOrder => {
  if (workOrder.category) return workOrder.category
  const category = data.categoryList.find(item => item.id === workOrder.categoryId)
  return category?.category || ''
}

/**
 * 读取认领员工缓存，并按当前员工选项类型进行归一化。
 */
const getCachedClaimEmployeeId = () => {
  const cachedEmployeeId = sessionStorage.getItem(CLAIM_EMPLOYEE_CACHE_KEY)
  if (!cachedEmployeeId) return ''

  const matchedEmployee = data.employeeList.find(item => String(item.id) === String(cachedEmployeeId))
  return matchedEmployee ? matchedEmployee.id : ''
}

/**
 * 写入认领员工缓存。
 */
const setCachedClaimEmployeeId = employeeId => {
  if (employeeId) {
    sessionStorage.setItem(CLAIM_EMPLOYEE_CACHE_KEY, String(employeeId))
  } else {
    sessionStorage.removeItem(CLAIM_EMPLOYEE_CACHE_KEY)
  }
}

/**
 * 清除认领员工缓存（切页/返回首页时调用）。
 */
const clearClaimEmployeeCache = () => {
  sessionStorage.removeItem(CLAIM_EMPLOYEE_CACHE_KEY)
}

/**
 * 打开认领弹窗，并回填上次选择员工。
 */
const openClaimDialog = workOrder => {
  Object.assign(claimFormModel, {
    ...claimFormInit,
    employeeId: getCachedClaimEmployeeId(),
    workOrderId: workOrder.id,
    factoryId: workOrder.factoryId,
    factoryName: resolveFactoryName(workOrder),
    number: workOrder.number,
    styleNumber: workOrder.styleNumber,
    categoryId: workOrder.categoryId,
    category: resolveCategoryName(workOrder),
    quantity: workOrder.quantity,
    createdDate: workOrder.createdDate
  })
  data.claimDialogVisible = true
}

/**
 * 关闭认领弹窗，仅重置工单字段，保留员工选择。
 */
const closeClaimDialog = () => {
  const selectedEmployeeId = claimFormModel.employeeId
  resetReactiveForm(claimFormModel, claimFormInit, claimFormRef)
  claimFormModel.employeeId = selectedEmployeeId
  data.claimDialogVisible = false
}

const submitClaimWorkOrder = async formEl => {
  await validateDialogForm(
    formEl,
    0,
    async () => {
      const { data: res } = await claimWorkOrder(
        claimFormModel.workOrderId,
        claimFormModel.employeeId,
        Number(claimFormModel.quantity)
      )

      if (res.status === 200) {
        ElMessage.success('认领成功')
        closeClaimDialog()
        await fetchWorkOrderList()
      } else {
        ElMessage.error(res.message)
      }
    },
    null,
    () => ElMessage.error('请检查是否填写正确')
  )
}

const validateWorkOrderForm = async formEl => {
  await validateDialogForm(
    formEl,
    data.workOrderDialogMode,
    createWorkOrder,
    modifyWorkOrder,
    () => ElMessage.error('请检查是否填写正确')
  )
}

const onlyNumberRule = {
  pattern: /^[0-9]+$/,
  message: '只能输入数字',
  trigger: 'blur'
}

const workOrderFormRules = reactive({
  factoryId: [{ required: true, message: '请选择工厂', trigger: 'change' }],
  number: [{ required: true, message: '请输入床号', trigger: 'blur' }],
  styleNumber: [{ required: true, message: '请输入款式编号', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择工作类型', trigger: 'change' }],
  quantity: [
    { required: true, message: '请输入数量', trigger: 'blur' },
    onlyNumberRule
  ],
  createdDate: [{ required: true, message: '请选择工单日期', trigger: 'change' }]
})

const claimFormRules = reactive({
  employeeId: [{ required: true, message: '请选择员工', trigger: 'change' }],
  quantity: [
    { required: true, message: '请输入数量', trigger: 'blur' },
    onlyNumberRule
  ]
})

/**
 * 监听员工选择并缓存，用于同页面多次打开弹窗时自动回填。
 */
watch(
  () => claimFormModel.employeeId,
  value => setCachedClaimEmployeeId(value)
)

/**
 * 切换路由离开当前页面时清理缓存。
 */
onBeforeRouteLeave(() => {
  clearClaimEmployeeCache()
})
</script>

<style scoped>
.workOrder-container {
  --card-bg: #ffffff;
  --card-border: #e9edf5;
  --card-shadow: 0 10px 28px rgba(16, 24, 40, 0.06);
  --text-primary: #1f2937;
  --text-secondary: #667085;

  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: clamp(12px, 2.2vw, 24px);
  gap: clamp(10px, 1.6vw, 18px);
  box-sizing: border-box;
  overflow: hidden;
  background: #f6f8fc;
}

.page-header-card,
.panel-card {
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  border-radius: 14px;
  box-shadow: var(--card-shadow);
}

.page-header-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: clamp(14px, 2vw, 20px);
}

.page-title {
  margin: 0;
  font-size: clamp(18px, 2vw, 24px);
  line-height: 1.25;
  font-weight: 700;
  color: var(--text-primary);
}

.page-subtitle {
  margin: 8px 0 0;
  font-size: clamp(12px, 1.2vw, 14px);
  color: var(--text-secondary);
}

.page-stat {
  display: inline-flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  min-width: 120px;
}

.page-stat .label {
  font-size: 12px;
  color: #98a2b3;
}

.page-stat .value {
  font-size: clamp(20px, 2.2vw, 28px);
  line-height: 1;
  font-weight: 700;
  color: #2563eb;
}

.panel-card {
  padding: clamp(12px, 1.8vw, 16px);
}

.workOrder-table-section {
  flex: 1;
  min-height: 0;
  display: flex;
  overflow: hidden;
}

.table-card {
  padding: clamp(8px, 1.2vw, 12px);
}

.workOrder-table-section > * {
  flex: 1;
  min-height: 0;
}

:deep(.panel-card .filter-bar),
:deep(.panel-card .common-filter-bar) {
  margin: 0;
}

:deep(.table-card .el-table) {
  border-radius: 10px;
}

:deep(.el-form-item__content) {
  flex-wrap: nowrap;
  gap: 10px;
}

:deep(.responsive-dialog .el-dialog) {
  border-radius: 14px;
}

:deep(.responsive-dialog .el-dialog__header) {
  margin-right: 0;
  padding: 16px 16px 8px;
}

:deep(.responsive-dialog .el-dialog__title) {
  font-weight: 700;
}

:deep(.responsive-dialog .el-dialog__body) {
  padding: 12px 16px;
}

:deep(.responsive-dialog .el-form-item) {
  margin-bottom: 16px;
}

:deep(.work-order-edit-dialog .el-input__wrapper),
:deep(.work-order-edit-dialog .el-select__wrapper),
:deep(.work-order-edit-dialog .el-date-editor.el-input__wrapper),
:deep(.claim-dialog .el-input__wrapper),
:deep(.claim-dialog .el-select__wrapper),
:deep(.claim-dialog .el-date-editor.el-input__wrapper) {
  min-height: 40px;
  border-radius: 9px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
}

.dialog-footer :deep(.el-button + .el-button) {
  margin-left: 0;
}

@media (max-width: 768px) {
  .workOrder-container {
    height: auto;
    min-height: 100%;
    padding: 12px;
    gap: 10px;
    overflow: visible;
  }

  .page-header-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .page-stat {
    width: 100%;
    align-items: flex-start;
  }

  .workOrder-table-section {
    overflow: visible;
  }

  :deep(.el-button),
  :deep(.el-select),
  :deep(.el-input),
  :deep(.el-date-editor) {
    width: 100%;
  }

  :deep(.panel-card .common-filter-bar),
  :deep(.panel-card .common-filter-bar .action-group),
  :deep(.panel-card .common-filter-bar .field-group) {
    width: 100%;
  }

  :deep(.panel-card .common-filter-bar .action-btn),
  :deep(.panel-card .common-filter-bar .reset-btn) {
    width: 100%;
    min-width: 0;
  }

  :deep(.responsive-dialog .el-dialog) {
    margin: 0 auto;
    border-radius: 12px;
  }

  :deep(.responsive-dialog .el-dialog__header) {
    margin-right: 0;
    padding: 14px 14px 8px;
  }

  :deep(.responsive-dialog .el-dialog__body) {
    padding: 8px 14px;
    max-height: 72vh;
    overflow-y: auto;
    box-sizing: border-box;
    -webkit-overflow-scrolling: touch;
  }

  :deep(.responsive-dialog .el-dialog__footer) {
    padding: 8px 14px 14px;
  }

  :deep(.responsive-dialog .el-form-item__content) {
    flex-wrap: wrap;
    gap: 6px;
  }

  :deep(.responsive-dialog .el-dialog__footer .el-button) {
    min-width: 92px;
    min-height: 40px;
  }
}
</style>
