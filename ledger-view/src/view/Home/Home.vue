<template>
  <FrontShell>
    <div class="admin-page">
      <div class="admin-page-head">
        <div>
          <h2 class="admin-page-head__title">工单总览</h2>
          <p class="admin-page-head__desc">
            查看并维护已认领的工单，按员工、成衣厂、工作类型和日期筛选；这里的数量与工资会计入当天的核算。
          </p>
        </div>
        <div class="admin-page-head__aside">
          <span class="admin-count-chip"><strong>{{ data.total }}</strong> 条工单</span>
          <el-button @click="openSampleDialog">查询样板</el-button>
          <el-button @click="clickSalary">计算工资</el-button>
          <el-button type="primary" @click="goClaimWorkOrderPage">认领工单</el-button>
        </div>
      </div>

      <div class="filter-card">
        <CommonFilterBar
          v-model="homeFilters"
          :fields="homeFilterFields"
          :initial-value="homeFilterInitialValue"
          @query="queryJobListByEmployeeIdAndDateHandle"
          @reset="onHomeFilterReset"
        />
      </div>

      <div class="table-card">
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
        :width="data.isMobile ? '94%' : '720px'"
        class="app-dialog sample-dialog"
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
            <el-option v-for="item in data.sampleStyleOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </div>

        <div class="sample-result-section">
          <el-empty v-if="!data.sampleQueried" description="选择款式编号后自动展示样板图片" :image-size="96" />
          <el-empty v-else-if="!data.sampleTableData.length" description="暂无匹配的样板图片" :image-size="96" />
          <div v-else class="sample-grid">
            <div v-for="item in data.sampleTableData" :key="`${item.styleNumber}-${item.imagePath}`" class="sample-card">
              <el-image
                :src="resolveImageUrl(item.imagePath)"
                :preview-src-list="[resolveImageUrl(item.imagePath)]"
                fit="cover"
                class="sample-image"
                preview-teleported
              >
                <template #error>
                  <div class="sample-image-empty">暂无图片</div>
                </template>
              </el-image>
              <div class="sample-style-number">{{ item.styleNumber }}</div>
            </div>
          </div>
        </div>
      </el-dialog>
    </div>
  </FrontShell>
</template>

<script setup>
import { reactive, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import FrontShell from './HomeComponent/FrontShell.vue'
import CommonFilterBar from './HomeComponent/CommonFilterBar.vue'
import HomeTable from './HomeComponent/HomeTable.vue'
import SalaryDialog from './HomeComponent/SalaryDialog.vue'
import JobEditDialog from './HomeComponent/JobEditDialog.vue'
import { confirmDelete } from '../Admin/adminShared.js'
import {
  queryEmployees,
  updateJobInfo,
  deleteJobInfoById,
  queryJobListByEmployeeIdAndDate,
  queryFactoryList,
  queryCategoryList,
  querySalaryByCondition,
  queryFactoryPieceRateSampleList
} from '../../network/index.js'

const updateMobileState = () => {
  data.isMobile = window.matchMedia('(max-width: 960px)').matches
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

const homeFilterFields = computed(() => [
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
    key: 'categoryId',
    type: 'select',
    placeholder: '工作类型',
    options: data.selectCategoryList,
    optionLabel: 'text',
    optionValue: 'value'
  },
  {
    key: 'employeeId',
    type: 'select',
    placeholder: '员工',
    options: data.selectEmployeeList,
    optionLabel: 'text',
    optionValue: 'value'
  },
  {
    type: 'date-range',
    keys: ['startDate', 'endDate'],
    placeholders: ['开始日期', '结束日期'],
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

const deleteJobInfoByIdHandler = async row => {
  const confirmed = await confirmDelete(`确定删除「${row.name || '该员工'} · ${row.styleNumber || '该款式'}」这条工单吗？`)
  if (!confirmed) return
  const { data: res } = await deleteJobInfoById(row.id)
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
 * 加载样板款式编号选项（仅flag=0数据）。
 */
const loadSampleStyleOptions = async () => {
  const { data: res } = await queryFactoryPieceRateSampleList('', 0)
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

  const { data: res } = await queryFactoryPieceRateSampleList(selectedStyleNumber, 0)
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
    return `${baseUrl}/admin/factoryPieceRate/imageView/${relativePath}?v=${data.imageVersion}`
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
  max-height: 56vh;
  overflow-y: auto;
}

.sample-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(168px, 1fr));
  gap: 12px;
}

.sample-card {
  border: 1px solid var(--admin-card-border);
  border-radius: 12px;
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  background: #ffffff;
}

.sample-image {
  width: 100%;
  height: 148px;
  border-radius: 8px;
  background: #f2f5fa;
}

.sample-image-empty {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  color: var(--admin-text-muted);
  font-size: 12px;
}

.sample-style-number {
  font-size: 12.5px;
  font-weight: 600;
  color: #1f2a44;
  text-align: center;
  word-break: break-all;
}

@media (max-width: 960px) {
  .sample-query-form {
    flex-direction: column;
    align-items: stretch;
  }

  .sample-result-section {
    max-height: 62vh;
  }
}
</style>
