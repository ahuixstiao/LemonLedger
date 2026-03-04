<template>
  <div class="workOrder-container">
    <CommonFilterBar
      v-model="workOrderFilters"
      :actions="workOrderFilterActions"
      :fields="workOrderFilterFields"
      :initial-value="workOrderFilterInitialValue"
      @action="onWorkOrderFilterAction"
      @query="fetchWorkOrderList"
      @reset="onWorkOrderFilterReset"
    />

    <div ref="tableSectionRef" class="workOrder-table-section">
      <el-table
        :data="data.tableData"
        class="workOrder-table"
        style="width: 100%"
        :max-height="tableMaxHeight"
        table-layout="fixed"
        stripe
        fit
        highlight-current-row
        empty-text="暂无数据"
      >
        <el-table-column label="认领" align="center" width="90">
          <template #default="scope">
            <el-button type="warning" @click="openClaimDialog(scope.row)">认领</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="factoryName" sortable label="厂名" align="center" show-overflow-tooltip />
        <el-table-column prop="number" sortable label="床号" align="center" show-overflow-tooltip />
        <el-table-column prop="styleNumber" sortable label="款式编号" align="center" show-overflow-tooltip />
        <el-table-column prop="category" sortable label="工作类型" align="center" show-overflow-tooltip />
        <el-table-column prop="quantity" label="数量" align="center" />
        <el-table-column prop="createdDate" sortable label="日期" align="center" show-overflow-tooltip />
        <el-table-column label="操作" align="center" width="110">
          <template #default="scope">
            <el-popconfirm title="确认删除?" @confirm="removeWorkOrder(scope.row.id)">
              <template #reference>
                <el-button type="danger" text>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="workOrder-page">
        <el-pagination
          background
          :total="data.total"
          v-model:current-page="data.currentPage"
          v-model:page-size="data.pageSize"
          :page-sizes="[5, 10, 20, 50, 100]"
          layout="sizes, prev, pager, next, jumper, ->"
          @current-change="fetchWorkOrderList"
          @size-change="fetchWorkOrderList"
        />
      </div>
    </div>

    <el-dialog
      v-model="data.editWorkOrderDialogVisible"
      title="添加工单"
      :width="data.isMobile ? '92%' : '50%'"
      :top="data.isMobile ? '4vh' : '12vh'"
      center
      class="responsive-dialog"
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
          <el-button type="primary" @click="validateWorkOrderForm(workOrderFormRef)">添加</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="data.claimDialogVisible"
      title="认领工单"
      :width="data.isMobile ? '92%' : '50%'"
      :top="data.isMobile ? '4vh' : '12vh'"
      center
      class="responsive-dialog"
    >
      <el-form
        ref="claimFormRef"
        :model="claimFormModel"
        :rules="claimFormRules"
        label-width="auto"
        :label-position="data.isMobile ? 'top' : 'left'"
      >
        <el-form-item size="large" label="员工:" prop="employeeId">
          <el-select filterable v-model="claimFormModel.employeeId" placeholder="请选择员工姓名">
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
import { onMounted, onBeforeUnmount, reactive, ref, computed, onUpdated } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import CommonFilterBar from './components/CommonFilterBar.vue'
import {
  queryEmployees,
  queryFactoryList,
  claimWorkOrder,
  deleteWorkOrder,
  queryWorkOrderList,
  saveWorkOrder
} from '../network/index.js'
import { queryFactoryJobCategoryList } from '../network/admin/index.js'
import {
  loadFactoryOptions,
  loadFactoryCategoryOptions,
  openCreateDialog,
  resetReactiveForm,
  validateDialogForm
} from './Admin/AdminComponent/factoryCrudShared.js'

const tableSectionRef = ref(null)
const tableMaxHeight = ref(360)

const calcTableMaxHeight = () => {
  const sectionEl = tableSectionRef.value
  if (!sectionEl) return
  const sectionHeight = sectionEl.clientHeight
  const pagerEl = sectionEl.querySelector('.workOrder-page')
  const pagerHeight = pagerEl ? pagerEl.offsetHeight : 0
  tableMaxHeight.value = Math.max(180, Math.floor(sectionHeight - pagerHeight))
}

const updateMobileState = () => {
  data.isMobile = window.matchMedia('(max-width: 768px)').matches
}

onMounted(() => {
  updateMobileState()
  calcTableMaxHeight()
  window.addEventListener('resize', updateMobileState)
  window.addEventListener('resize', calcTableMaxHeight)
  loadFactoryList()
  loadCategoryList()
  loadEmployeeList()
  fetchWorkOrderList()
})

onUpdated(() => {
  calcTableMaxHeight()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateMobileState)
  window.removeEventListener('resize', calcTableMaxHeight)
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
    clearable: true,
    options: data.categoryList,
    optionLabel: 'category',
    optionValue: 'id'
  },
  {
    key: 'startDate',
    type: 'date',
    placeholder: '开始日期',
    popperClass: 'month-only-picker'
  },
  {
    key: 'endDate',
    type: 'date',
    placeholder: '结束日期',
    popperClass: 'month-only-picker'
  }
])

const goHome = () => {
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
  await loadFactoryCategoryOptions(data, queryFactoryJobCategoryList)
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

const createWorkOrder = async () => {
  const { data: res } = await saveWorkOrder(workOrderFormModel)
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

const openCreateWorkOrderDialog = () => {
  openCreateDialog(
    workOrderFormModel,
    workOrderFormInit,
    data,
    null,
    'editWorkOrderDialogVisible'
  )
}

const closeWorkOrderDialog = () => {
  resetReactiveForm(workOrderFormModel, workOrderFormInit, workOrderFormRef)
  data.editWorkOrderDialogVisible = false
}

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

const openClaimDialog = workOrder => {
  Object.assign(claimFormModel, {
    ...claimFormInit,
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

const closeClaimDialog = () => {
  resetReactiveForm(claimFormModel, claimFormInit, claimFormRef)
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
    0,
    createWorkOrder,
    null,
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
</script>

<style scoped>
:deep(.workOrder-table .cell) {
  white-space: nowrap;
}

.workOrder-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  padding: var(--section-padding-lg);
  gap: var(--layout-gap-lg);
  box-sizing: border-box;
  overflow: hidden;
}

.workOrder-table-section {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.workOrder-table {
  flex: 1;
  min-height: 0;
}

.workOrder-page {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 12px 0 4px;
  flex-shrink: 0;
}

:deep(.el-pagination) {
  flex-wrap: wrap;
  justify-content: center;
  row-gap: 8px;
}

:deep(.el-form-item__content) {
  flex-wrap: nowrap;
  gap: 10px;
}

:deep(.responsive-dialog .el-dialog__body) {
  padding: 16px;
}

:deep(.responsive-dialog .el-form-item) {
  margin-bottom: 16px;
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
    min-height: auto;
    padding: var(--section-padding-sm);
    gap: var(--layout-gap-sm);
    overflow: visible;
  }

  .workOrder-table-section {
    overflow: visible;
  }

  .workOrder-page {
    justify-content: center;
  }

  :deep(.el-button),
  :deep(.el-select),
  :deep(.el-input),
  :deep(.el-date-editor) {
    width: 100%;
  }

  :deep(.responsive-dialog .el-dialog) {
    margin: 0 auto;
    border-radius: 10px;
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
  }

  :deep(.responsive-dialog .el-dialog__footer) {
    padding: 8px 14px 14px;
  }

  :deep(.responsive-dialog .el-form-item__content) {
    flex-wrap: wrap;
    gap: 6px;
  }

  :deep(.responsive-dialog .el-dialog__footer .el-button) {
    min-width: 88px;
  }
}
</style>
