<template>
  <FrontShell>
    <div class="admin-page">
      <div class="admin-page-head">
        <div>
          <h2 class="admin-page-head__title">认领工单</h2>
          <p class="admin-page-head__desc">
            按成衣厂、床号、款式和日期筛选待认领工序；认领后可随时回到工单总览里修改。
          </p>
        </div>
        <div class="admin-page-head__aside">
          <span class="admin-count-chip"><strong>{{ data.total }}</strong> 条工单</span>
          <el-button type="primary" @click="openCreateWorkOrderDialog">添加工单</el-button>
        </div>
      </div>

      <div class="filter-card">
        <CommonFilterBar
          v-model="workOrderFilters"
          :fields="workOrderFilterFields"
          :initial-value="workOrderFilterInitialValue"
          @query="fetchWorkOrderList"
          @reset="onWorkOrderFilterReset"
        />
      </div>

      <div class="table-card">
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
      </div>
    <!--  添加/编辑工单弹窗  -->
    <el-dialog
      v-model="data.editWorkOrderDialogVisible"
      :title="data.workOrderDialogMode === 0 ? '添加工单' : '编辑工单'"
      :width="data.isMobile ? '94%' : '480px'"
      class="app-dialog"
    >
      <el-form
        ref="workOrderFormRef"
        :model="workOrderFormModel"
        :rules="workOrderFormRules"
        label-position="top"
      >
        <el-form-item label="成衣厂" prop="factoryId">
          <el-select filterable v-model="workOrderFormModel.factoryId" placeholder="选择成衣厂">
            <el-option v-for="item in data.factoryList" :key="item.id" :label="item.factoryName" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="床号" prop="number">
          <el-input v-model="workOrderFormModel.number" placeholder="输入床号" />
        </el-form-item>

        <el-form-item label="款式编号" prop="styleNumber">
          <el-input v-model="workOrderFormModel.styleNumber" placeholder="输入款式编号" />
        </el-form-item>

        <el-form-item label="工作类型" prop="categoryId">
          <el-select v-model="workOrderFormModel.categoryId" placeholder="选择工作类型">
            <el-option v-for="item in data.categoryList" :key="item.id" :label="item.category" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="数量" prop="quantity">
          <el-input v-model="workOrderFormModel.quantity" placeholder="输入数量" />
        </el-form-item>

        <el-form-item label="工单日期" prop="createdDate">
          <el-date-picker
            :editable="false"
            v-model="workOrderFormModel.createdDate"
            type="date"
            format="YYYY/MM/DD"
            value-format="YYYY-MM-DD"
            placeholder="工单日期"
            style="width: 100%"
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
      :width="data.isMobile ? '94%' : '460px'"
      class="app-dialog"
    >
      <el-form
        ref="claimFormRef"
        :model="claimFormModel"
        :rules="claimFormRules"
        label-position="top"
      >
        <el-form-item label="员工" prop="employeeId">
          <el-select v-model="claimFormModel.employeeId" placeholder="请选择员工姓名">
            <el-option v-for="item in data.employeeList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="厂名">
          <el-input :model-value="claimFormModel.factoryName" disabled />
        </el-form-item>

        <el-form-item label="床号">
          <el-input :model-value="claimFormModel.number" disabled />
        </el-form-item>

        <el-form-item label="数量" prop="quantity">
          <el-input v-model="claimFormModel.quantity" placeholder="请输入数量" />
        </el-form-item>

        <!-- 员工没有默认工作方式时必须让他选：同一个人压花、刮胶都会做 -->
        <el-form-item v-if="data.claimModeRequired" label="工作方式" prop="modeId">
          <el-select v-model="claimFormModel.modeId" placeholder="请选择本次按哪种方式算工资">
            <el-option v-for="item in data.modeList" :key="item.id" :label="item.mode" :value="item.id" />
          </el-select>
        </el-form-item>

        <!-- 认领前先看这道工序还剩多少，不然只能提交了才知道超额 -->
        <el-form-item v-if="data.claimProgress" label="认领情况">
          <span class="claim-progress">
            工单数量 {{ data.claimProgress.quantity }}，已认领 {{ data.claimProgress.claimedQuantity }}，
            <strong>还可认领 {{ data.claimProgress.remainQuantity }}</strong>
          </span>
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
  </FrontShell>
</template>

<script setup>
import { onMounted, onBeforeUnmount, reactive, ref, computed, watch } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import { ElMessage } from 'element-plus'
import FrontShell from './HomeComponent/FrontShell.vue'
import CommonFilterBar from './HomeComponent/CommonFilterBar.vue'
import WorkOrderTable from './HomeComponent/WorkOrderTable.vue'
import {
  queryEmployees,
  queryModeList,
  queryFactoryList,
  claimWorkOrder,
  deleteWorkOrder,
  queryWorkOrderList,
  queryWorkOrderProgress,
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
import { confirmDelete } from '../Admin/adminShared.js'

const updateMobileState = () => {
  data.isMobile = window.matchMedia('(max-width: 960px)').matches
}

onMounted(() => {
  updateMobileState()
  window.addEventListener('resize', updateMobileState)
  loadFactoryList()
  loadCategoryList()
  loadEmployeeList()
  loadModeList()
  fetchWorkOrderList()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateMobileState)
})

const data = reactive({
  tableData: [],
  factoryList: [],
  categoryList: [],
  employeeList: [],
  modeList: [],
  claimProgress: null,
  claimModeRequired: false,
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

const workOrderFilterFields = computed(() => [
  {
    key: 'factoryId',
    type: 'select',
    placeholder: '选择成衣厂',
    filterable: true,
    clearable: true,
    wide: true,
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
    placeholder: '工作类型',
    clearable: true,
    options: data.categoryList,
    optionLabel: 'category',
    optionValue: 'id'
  },
  {
    type: 'date-range',
    keys: ['startDate', 'endDate'],
    placeholders: ['开始日期', '结束日期'],
    popperClass: 'month-only-picker'
  }
])

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

const loadModeList = async () => {
  const { data: res } = await queryModeList()
  data.modeList = res?.data || []
}

/**
 * 认领弹窗里的「还可认领多少」。
 * 员工有默认工作方式就用员工的，没有就用弹窗里选的那个。
 */
const loadClaimProgress = async () => {
  const { workOrderId, employeeId, modeId } = claimFormModel
  if (!workOrderId || (!employeeId && !modeId)) {
    data.claimProgress = null
    return
  }
  try {
    const { data: res } = await queryWorkOrderProgress(workOrderId, employeeId, modeId || undefined)
    data.claimProgress = res?.status === 200 ? res.data : null
  } catch (error) {
    // 只是提示性信息，拿不到就不显示，不要拦住认领
    data.claimProgress = null
  }
}

/**
 * 按选中的员工决定要不要让用户选工作方式。
 */
const syncClaimModeState = () => {
  const employee = data.employeeList.find(item => String(item.id) === String(claimFormModel.employeeId))
  data.claimModeRequired = Boolean(employee) && !employee.modeId
  if (!data.claimModeRequired) {
    // 员工有默认方式时，后端会自己用员工的 modeId，这里清掉避免提交多余参数
    claimFormModel.modeId = ''
  }
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

const removeWorkOrder = async row => {
  const confirmed = await confirmDelete(`确定删除「${row.factoryName || '该厂'} · ${row.styleNumber || '该款式'}」这条工单吗？`)
  if (!confirmed) return
  const { data: res } = await deleteWorkOrder(row.id)
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
  modeId: '',
  factoryId: '',
  factoryName: '',
  number: '',
  quantity: '',
  workOrderId: ''
}
const claimFormModel = reactive({ ...claimFormInit })

const resolveFactoryName = workOrder => {
  if (workOrder.factoryName) return workOrder.factoryName
  const factory = data.factoryList.find(item => item.id === workOrder.factoryId)
  return factory?.factoryName || ''
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
    quantity: workOrder.quantity
  })
  data.claimProgress = null
  syncClaimModeState()
  data.claimDialogVisible = true
  loadClaimProgress()
}

/**
 * 关闭认领弹窗，仅重置工单字段，保留员工选择。
 */
const closeClaimDialog = () => {
  const selectedEmployeeId = claimFormModel.employeeId
  resetReactiveForm(claimFormModel, claimFormInit, claimFormRef)
  claimFormModel.employeeId = selectedEmployeeId
  data.claimProgress = null
  data.claimModeRequired = false
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
        Number(claimFormModel.quantity),
        claimFormModel.modeId || undefined
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
  modeId: [
    {
      validator: (rule, value, callback) => {
        // 只有员工没填默认工作方式时才必填
        if (data.claimModeRequired && !value) {
          callback(new Error('请选择本次的工作方式'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ],
  quantity: [
    { required: true, message: '请输入数量', trigger: 'blur' },
    onlyNumberRule
  ]
})

/**
 * 监听员工选择并缓存，用于同页面多次打开弹窗时自动回填；
 * 同时刷新「要不要选工作方式」和「还可认领多少」。
 */
watch(
  () => claimFormModel.employeeId,
  value => {
    setCachedClaimEmployeeId(value)
    syncClaimModeState()
    loadClaimProgress()
  }
)

/**
 * 换了工作方式，剩余量也要跟着重算（不同工序分别计）。
 */
watch(
  () => claimFormModel.modeId,
  () => loadClaimProgress()
)

/**
 * 切换路由离开当前页面时清理缓存。
 */
onBeforeRouteLeave(() => {
  clearClaimEmployeeCache()
})
</script>

<style scoped>
.claim-progress {
  display: block;
  font-size: 13px;
  line-height: 1.7;
  color: var(--admin-text-secondary);
}

.claim-progress strong {
  color: #d97706;
  font-weight: 600;
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
</style>
