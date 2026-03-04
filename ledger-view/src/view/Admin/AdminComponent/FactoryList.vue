<template>
  <div class="factoryList-container">
    <div class="factoryList-toolbar">
      <el-button type="primary" @click="openCreateFactoryDialog">添加工厂</el-button>
      <el-input
        v-model="data.factoryName"
        clearable
        placeholder="工厂名称"
        @input="fetchFactoryList"
      />
      <el-select v-model="data.flag" placeholder="状态" @change="fetchFactoryList">
        <el-option label="正常" :value="0" />
        <el-option label="删除" :value="1" />
      </el-select>
      <el-button type="warning" plain class="toolbar-reset-btn" @click="resetFactoryFilter">重置</el-button>
    </div>

    <el-table :data="data.tableData" stripe fit highlight-current-row empty-text="暂无数据">
      <el-table-column prop="id" label="ID" align="center" width="80" sortable />
      <el-table-column prop="factoryName" label="工厂名称" align="center" sortable />
      <el-table-column prop="createdDate" label="创建日期" align="center" sortable />
      <el-table-column prop="flag" label="状态" align="center" width="100" sortable>
        <template #default="scope">
          <el-tag v-if="scope.row.flag === 0" type="success">正常</el-tag>
          <el-tag v-else type="danger">删除</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160">
        <template #default="scope">
          <template v-if="isReadOnlyRow(scope.row)">
            <span class="readonly-text">已删除</span>
          </template>
          <template v-else>
            <el-button text @click="openEditFactoryDialog(scope.row)">编辑</el-button>
            <el-button type="danger" text @click="removeFactory(scope.row.id)">删除</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <div class="factoryList-page">
      <el-pagination
        background
        :total="data.total"
        v-model:current-page="data.currentPage"
        v-model:page-size="data.pageSize"
        :page-sizes="[5, 10, 20, 50, 100]"
        layout="sizes, prev, pager, next, jumper, ->"
        @current-change="fetchFactoryList"
        @size-change="fetchFactoryList"
      />
    </div>

    <el-dialog
      v-model="data.editFactoryDialogVisible"
      :title="data.editFactoryDialogMode === 0 ? '添加工厂' : '修改工厂'"
      width="40%"
      center
    >
      <el-form
        ref="factoryFormRef"
        :model="factoryFormRefModel"
        :rules="factoryFormRules"
        label-position="left"
        label-width="auto"
      >
        <el-form-item label="工厂名称" prop="factoryName">
          <el-input v-model="factoryFormRefModel.factoryName" placeholder="请输入工厂名称" />
        </el-form-item>
        <el-form-item label="创建日期" prop="createdDate">
          <el-date-picker
            v-model="factoryFormRefModel.createdDate"
            type="date"
            format="YYYY/MM/DD"
            value-format="YYYY-MM-DD"
            placeholder="请选择创建日期"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeFactoryDialog">取消</el-button>
        <el-button type="primary" @click="validateFactoryForm(factoryFormRef)">
          {{ data.editFactoryDialogMode === 0 ? '新增' : '修改' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  deleteAdminFactory,
  queryAdminFactoryList,
  saveAdminFactory,
  updateAdminFactory
} from '../../../network/admin/index.js'
import {
  openCreateDialog,
  openEditDialog,
  resetReactiveForm,
  validateDialogForm
} from './factoryCrudShared.js'

onMounted(() => {
  fetchFactoryList()
})

const data = reactive({
  factoryName: '',
  flag: 0,
  tableData: [],
  total: 0,
  currentPage: 1,
  pageSize: 10,
  editFactoryDialogVisible: false,
  editFactoryDialogMode: 0
})

const fetchFactoryList = async () => {
  const { data: res } = await queryAdminFactoryList(
    (data.factoryName || '').trim(),
    data.currentPage,
    data.pageSize,
    data.flag
  )

  if (res.status !== 200) {
    data.tableData = []
    data.total = 0
    ElMessage.error(res.message)
    return
  }

  const records = res?.data?.records
  if (Array.isArray(records)) {
    data.tableData = records
    data.total = res?.data?.total || 0
    return
  }

  const list = Array.isArray(res?.data) ? res.data : []
  data.tableData = list
  data.total = list.length
}

const createFactory = async () => {
  const { data: res } = await saveAdminFactory(factoryFormRefModel)
  if (res.status === 200) {
    ElMessage.success(res.message)
    closeFactoryDialog()
    await fetchFactoryList()
  } else {
    ElMessage.error(res.message)
  }
}

const updateFactory = async () => {
  const { data: res } = await updateAdminFactory(factoryFormRefModel)
  if (res.status === 200) {
    ElMessage.success(res.message)
    closeFactoryDialog()
    await fetchFactoryList()
  } else {
    ElMessage.error(res.message)
  }
}

const removeFactory = async id => {
  const { data: res } = await deleteAdminFactory(id)
  if (res.status === 200) {
    ElMessage.success(res.message)
    await fetchFactoryList()
  } else {
    ElMessage.error(res.message)
  }
}

const factoryFormRef = ref()
const factoryFormInit = { id: '', factoryName: '', createdDate: '' }
const factoryFormRefModel = reactive({ ...factoryFormInit })

const openCreateFactoryDialog = () => {
  openCreateDialog(
    factoryFormRefModel,
    factoryFormInit,
    data,
    'editFactoryDialogMode',
    'editFactoryDialogVisible'
  )
}

const openEditFactoryDialog = factory => {
  openEditDialog(factoryFormRefModel, factory, data, 'editFactoryDialogMode', 'editFactoryDialogVisible')
}

const closeFactoryDialog = () => {
  resetReactiveForm(factoryFormRefModel, factoryFormInit, factoryFormRef)
  data.editFactoryDialogVisible = false
}

const validateFactoryForm = async formEl => {
  await validateDialogForm(
    formEl,
    data.editFactoryDialogMode,
    createFactory,
    updateFactory,
    () => ElMessage.error('请检查是否填写正确')
  )
}

const resetFactoryFilter = () => {
  data.factoryName = ''
  data.flag = 0
  data.currentPage = 1
  fetchFactoryList()
}

/**
 * 删除状态的数据仅允许展示，不提供编辑/删除入口。
 */
const isReadOnlyRow = row => {
  return Number(row?.flag) === 1 || Number(data.flag) === 1
}

const factoryFormRules = reactive({
  factoryName: [{ required: true, message: '请输入工厂名称', trigger: 'blur' }],
  createdDate: [{ required: true, message: '请选择创建日期', trigger: 'change' }]
})
</script>

<style scoped>
.factoryList-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.factoryList-toolbar {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  padding: 12px;
  gap: 12px;
  border-bottom: 1px solid var(--el-border-color);
  overflow-x: auto;
  overflow-y: hidden;
}

.factoryList-toolbar > *:nth-child(1) {
  width: 96px;
}

.factoryList-toolbar > *:nth-child(2) {
  width: 200px;
}

.factoryList-toolbar > *:nth-child(3) {
  width: 130px;
}

.factoryList-toolbar > *:nth-child(4) {
  width: 96px;
}

.factoryList-toolbar > * {
  flex-shrink: 0;
}

.factoryList-toolbar :deep(.el-input__wrapper),
.factoryList-toolbar :deep(.el-select__wrapper) {
  min-height: 32px;
}

.factoryList-page {
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  padding: 25px 0;
}

:deep(.el-table--fit) {
  flex: auto;
}

.readonly-text {
  color: var(--el-text-color-placeholder);
}
</style>