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
      <HomeTable
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

    <el-dialog
      v-model="data.sampleDialogVisible"
      title="查询样板"
      :width="data.isMobile ? '92%' : '50%'"
      :top="data.isMobile ? '4vh' : '12vh'"
      center
      class="responsive-dialog"
    >
      <div class="sample-query-form">
        <el-select
          v-model="data.sampleStyleNumber"
          filterable
          allow-create
          default-first-option
          clearable
          placeholder="输入或选择款式编号"
          class="sample-input"
          @change="onSampleStyleChange"
        >
          <el-option
            v-for="item in data.sampleStyleOptions"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select>
      </div>

      <div class="sample-result-section">
        <el-empty v-if="!data.sampleQueried" description="请选择款式编号后查询样板图片" />
        <el-empty v-else-if="!data.sampleTableData.length" description="暂无匹配样板图片" />
        <div v-else class="sample-grid">
          <div v-for="item in data.sampleTableData" :key="`${item.styleNumber}-${item.imagePath}`" class="sample-card">
            <div class="sample-style-number">{{ item.styleNumber }}</div>
            <el-image
              :src="resolveImageUrl(item.imagePath)"
              :preview-src-list="[resolveImageUrl(item.imagePath)]"
              fit="cover"
              class="sample-image"
              preview-teleported
            />
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import CommonFilterBar from './HomeComponent/CommonFilterBar.vue'
import HomeTable from './HomeComponent/HomeTable.vue'
import SalaryDialog from './HomeComponent/SalaryDialog.vue'
import JobEditDialog from './HomeComponent/JobEditDialog.vue'
import {
  queryEmployees,
  updateJobInfo,
  deleteJobInfoById,
  queryJobListByEmployeeIdAndDate,
  queryFactoryList,
  queryCategoryList,
  querySalaryByCondition,
  queryFactoryQuotationSampleList
} from '../../network/index.js'

const updateMobileState = () => {
  data.isMobile = window.matchMedia('(max-width: 768px)').matches
}

onMounted(() => {
  updateMobileState()
  window.addEventListener('resize', updateMobileState)
  queryEmployeeListHandle()
  queryFactoryListHandle()
  queryCategoryListHandle()
  queryJobListByEmployeeIdAndDateHandle()
  loadSampleStyleOptions()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateMobileState)
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
  pageSize: 10,
  sampleDialogVisible: false,
  sampleStyleNumber: '',
  sampleTableData: [],
  sampleStyleOptions: [],
  sampleQueried: false,
  isMobile: false,
  imageVersion: 0
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
  { key: 'sampleQuery', label: '查询样板', type: 'primary' },
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
    filterable: false,
    options: data.selectCategoryList,
    optionLabel: 'text',
    optionValue: 'value'
  },
  {
    key: 'employeeId',
    type: 'select',
    placeholder: '选择员工',
    filterable: false,
    options: data.selectEmployeeList,
    optionLabel: 'text',
    optionValue: 'value'
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

/**
 * 打开工资计算弹窗。
 */
const clickSalary = () => {
  data.SalaryVisible = true
}

/**
 * 打开样板查询弹窗。
 */
const openSampleDialog = () => {
  data.sampleDialogVisible = true
  data.sampleQueried = false
  data.sampleStyleNumber = ''
  data.sampleTableData = []
}

/**
 * 筛选栏动作处理。
 */
const onHomeFilterAction = action => {
  if (action === 'claimWorkOrder') {
    goClaimWorkOrderPage()
    return
  }
  if (action === 'sampleQuery') {
    openSampleDialog()
    return
  }
  if (action === 'salary') {
    clickSalary()
  }
}

/**
 * 加载样板款式编号选项（仅flag=0数据）。
 */
const loadSampleStyleOptions = async () => {
  const { data: res } = await queryFactoryQuotationSampleList('', 0)
  if (res.status === 200) {
    const sourceList = Array.isArray(res.data) ? res.data : []
    const styleSet = new Set(sourceList.map(item => item.styleNumber).filter(Boolean))
    data.sampleStyleOptions = Array.from(styleSet)
    return
  }
  data.sampleStyleOptions = []
}

/**
 * 查询样板数据（仅款式编号和图片）。
 */
const fetchSampleList = async () => {
  const selectedStyleNumber = String(data.sampleStyleNumber || '').trim()
  if (!selectedStyleNumber) {
    data.sampleQueried = false
    data.sampleTableData = []
    return
  }

  const { data: res } = await queryFactoryQuotationSampleList(selectedStyleNumber, 0)
  data.sampleQueried = true
  if (res.status === 200) {
    data.sampleTableData = Array.isArray(res.data) ? res.data : []
  } else {
    data.sampleTableData = []
    ElMessage.error(res.message)
  }
}

/**
 * 款式编号变更后自动触发查询。
 */
const onSampleStyleChange = () => {
  fetchSampleList()
}

/**
 * 将相对图片路径转换为可访问的完整URL。
 */
const resolveImageUrl = imagePath => {
  if (!imagePath) {
    return ''
  }

  const normalizedPath = String(imagePath)
  if (normalizedPath.startsWith('http')) {
    return normalizedPath
  }

  const baseUrl = (import.meta.env.VITE_BASE_URL || '').replace(/\/$/, '')
  const imagePrefix = '/lemonLedgerImages/'

  if (normalizedPath.startsWith(imagePrefix)) {
    const relativePath = normalizedPath.slice(imagePrefix.length)
    return `${baseUrl}/admin/factoryQuotation/imageView/${relativePath}?v=${data.imageVersion}`
  }

  return `${baseUrl}${normalizedPath}?v=${data.imageVersion}`
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

.sample-query-form {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sample-input {
  flex: 1;
}

.sample-result-section {
  margin-top: 14px;
  max-height: 58vh;
  overflow-y: auto;
}

.sample-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 12px;
}

.sample-card {
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sample-style-number {
  font-size: 13px;
  color: var(--el-text-color-primary);
  word-break: break-all;
}

.sample-image {
  width: 100%;
  height: 140px;
  border-radius: 6px;
  border: 1px solid var(--el-border-color-light);
}

@media (max-width: 768px) {
  .home-container {
    height: auto;
    min-height: 100%;
    padding: var(--section-padding-sm);
    gap: var(--layout-gap-sm);
    overflow: visible;
  }

  .home-table-section {
    overflow: visible;
    min-height: auto;
  }

  .sample-query-form {
    flex-direction: column;
    align-items: stretch;
  }

  .sample-result-section {
    max-height: 64vh;
  }

  :deep(.responsive-dialog .el-dialog) {
    margin: 0 auto;
    border-radius: 10px;
  }

  :deep(.responsive-dialog .el-dialog__body) {
    padding: 10px 14px;
  }
}

</style>
