<template>
  <div class="home-container">
    <CommonFilterBar
      v-model="homeFilters"
      :actions="homeFilterActions"
      :fields="homeFilterFields"
      :initial-value="homeFilterInitialValue"
      @action="onHomeFilterAction"
      @query="queryJobListByEmployeeIdAndDateHandle"
      @reset="onHomeFilterReset"
    />

    <div class="home-table-section">
      <WorkRecordTable
        :table-data="data.tableData"
        :total="data.total"
        :current-page="data.currentPage"
        :page-size="data.pageSize"
        @update:currentPage="val => (data.currentPage = val)"
        @update:pageSize="val => (data.pageSize = val)"
        @query="queryJobListByEmployeeIdAndDateHandle"
        @edit="showUpdateJobInfoDialog"
        @delete="deleteJobInfoByIdHandler"
      />
    </div>

    <SalaryDialog
      :visible="data.SalaryVisible"
      :employee-list="data.employeeList"
      @update:visible="val => (data.SalaryVisible = val)"
      @submit="salaryInquiryHandle"
    />

    <JobEditDialog
      :visible="data.editJobDialogVisible"
      :dialog-mode="data.dialogMode"
      :loading="data.isLoading"
      :job-info="jobInfoRef"
      :employee-list="data.employeeList"
      :factory-list="data.factoryList"
      :category-list="data.categoryList"
      @update:visible="val => (data.editJobDialogVisible = val)"
      @employee-change="handleEmployeeChange"
      @cancel="handleCancelJobDialog"
      @submit="submitJobInfo"
    />
  </div>
</template>

<script setup>
import { reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import CommonFilterBar from './components/CommonFilterBar.vue'
import WorkRecordTable from './HomeComponent/WorkRecordTable.vue'
import SalaryDialog from './HomeComponent/SalaryDialog.vue'
import JobEditDialog from './HomeComponent/JobEditDialog.vue'
import {
  queryEmployees,
  updateJobInfo,
  deleteJobInfoById,
  queryJobListByEmployeeIdAndDate,
  queryFactoryList,
  queryCategoryList,
  querySalaryByCondition
} from '../network/index.js'

onMounted(() => {
  queryEmployeeListHandle()
  queryFactoryListHandle()
  queryCategoryListHandle()
  queryJobListByEmployeeIdAndDateHandle()
})

const router = useRouter()

const data = reactive({
  editJobDialogVisible: false,
  dialogMode: 1,
  SalaryVisible: false,
  id: 0,
  startDate: '',
  endDate: '',
  factoryId: '',
  number: '',
  categoryId: 0,
  employeeList: [],
  tableData: [],
  isLoading: false,
  factoryList: [],
  categoryList: [],
  selectCategoryList: [],
  selectEmployeeList: [],
  total: 0,
  currentPage: 1,
  pageSize: 10
})

const homeFilterInitialValue = {
  startDate: '',
  endDate: '',
  employeeId: 0,
  categoryId: 0,
  factoryId: '',
  number: ''
}

const homeFilters = computed({
  get: () => ({
    startDate: data.startDate,
    endDate: data.endDate,
    employeeId: data.id,
    categoryId: data.categoryId,
    factoryId: data.factoryId,
    number: data.number
  }),
  set: value => {
    data.startDate = value.startDate ?? ''
    data.endDate = value.endDate ?? ''
    data.id = value.employeeId ?? 0
    data.categoryId = value.categoryId ?? 0
    data.factoryId = value.factoryId ?? ''
    data.number = value.number ?? ''
  }
})

const homeFilterActions = [
  { key: 'claimWorkOrder', label: '认领工单', type: 'primary' },
  { key: 'salary', label: '计算工资', type: 'primary' }
]

const homeFilterFields = computed(() => [
  {
    key: 'factoryId',
    type: 'select',
    placeholder: '选择成衣厂',
    filterable: true,
    options: data.factoryList,
    optionLabel: 'factoryName',
    optionValue: 'id'
  },
  {
    key: 'number',
    type: 'input',
    placeholder: '要查询的床号'
  },
  {
    key: 'categoryId',
    type: 'select',
    placeholder: '选择工作类型',
    options: data.selectCategoryList,
    optionLabel: 'text',
    optionValue: 'value'
  },
  {
    key: 'employeeId',
    type: 'select',
    placeholder: '选择员工',
    filterable: true,
    options: data.selectEmployeeList,
    optionLabel: 'text',
    optionValue: 'value'
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

const queryEmployeeListHandle = async () => {
  const { data: res } = await queryEmployees()
  data.employeeList = res.data || []
  data.selectEmployeeList = data.employeeList.map(item => ({ text: item.name, value: item.id, modeId: item.modeId }))
  data.selectEmployeeList.unshift({ text: '选择员工', value: 0 })
}

const queryFactoryListHandle = async () => {
  const { data: res } = await queryFactoryList()
  data.factoryList = res.data || []
}

const queryCategoryListHandle = async () => {
  const { data: res } = await queryCategoryList()
  data.categoryList = res.data || []
  data.selectCategoryList = data.categoryList.map(item => ({ text: item.category, value: item.id }))
  data.selectCategoryList.unshift({ text: '选择工作类型', value: 0 })
}

const queryJobListByEmployeeIdAndDateHandle = async () => {
  const { data: res } = await queryJobListByEmployeeIdAndDate(
    data.id,
    data.startDate,
    data.endDate,
    data.factoryId,
    data.number,
    data.categoryId,
    data.currentPage,
    data.pageSize
  )
  if (res.status === 200) {
    data.tableData = res.data.records
    data.total = res.data.total
  } else {
    data.tableData = []
    ElMessage.error(res.message)
  }
}

const salaryInquiryHandle = async payload => {
  const { data: res } = await querySalaryByCondition(payload.employeeId, payload.startDate, payload.endDate)
  if (res.status === 200) {
    payload.onResult?.(res.data.salary)
    return
  }
  ElMessage.error(res.message)
  payload.onResult?.(0)
}

const goClaimWorkOrderPage = () => {
  router.push('/workOrder')
}

const updateJobInfoHandle = async () => {
  const { data: res } = await updateJobInfo(jobInfoRef)
  if (res.status === 200) {
    ElMessage.success(res.message)
    resetJobInfoFormData()
    data.editJobDialogVisible = false
    await queryJobListByEmployeeIdAndDateHandle()
  } else ElMessage.error(res.message)
}

const deleteJobInfoByIdHandler = async id => {
  const { data: res } = await deleteJobInfoById(id)
  if (res.status === 200) ElMessage.success(res.message)
  else ElMessage.error(res.message)
  await queryJobListByEmployeeIdAndDateHandle()
}

const handleEmployeeChange = employeeId => {
  const selectedEmployee = data.employeeList.find(item => item.id === employeeId)
  jobInfoRef.modeId = selectedEmployee ? selectedEmployee.modeId : ''
}

const clickSalary = () => {
  data.SalaryVisible = true
}

const onHomeFilterAction = action => {
  if (action === 'claimWorkOrder') {
    goClaimWorkOrderPage()
    return
  }
  if (action === 'salary') {
    clickSalary()
  }
}

const showUpdateJobInfoDialog = job => {
  data.dialogMode = 1
  Object.assign(jobInfoRef, job)
  data.editJobDialogVisible = true
}

const resetQueryCondition = () => {
  data.startDate = ''
  data.endDate = ''
  data.id = 0
  data.factoryId = ''
  data.number = ''
  data.categoryId = 0
  data.currentPage = 1
  queryJobListByEmployeeIdAndDateHandle()
}

const onHomeFilterReset = () => {
  resetQueryCondition()
}

const jobInfoInit = {
  id: '', employeeId: '', factoryId: '', categoryId: '', modeId: '', number: '', styleNumber: '', quantity: '', salary: '', createdDate: ''
}

const jobInfoRef = reactive({ ...jobInfoInit })

const resetJobInfoFormData = () => {
  Object.assign(jobInfoRef, jobInfoInit)
}

const handleCancelJobDialog = () => {
  resetJobInfoFormData()
  data.editJobDialogVisible = false
}

const submitJobInfo = () => {
  updateJobInfoHandle()
}
</script>

<style scoped>
.home-container {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: var(--section-padding-lg);
  box-sizing: border-box;
  gap: var(--layout-gap-lg);
  overflow: hidden;
}

.home-table-section {
  flex: 1;
  min-height: 0;
  display: flex;
  overflow: hidden;
}

.home-table-section > * {
  flex: 1;
  min-height: 0;
}

@media (max-width: 768px) {
  .home-container {
    height: auto;
    min-height: auto;
    padding: var(--section-padding-sm);
    gap: var(--layout-gap-sm);
    overflow: visible;
  }

  .home-table-section {
    overflow: visible;
  }
}
</style>
