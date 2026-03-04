<template>
  <div class="employee-container">
    <div class="employee-toolbar">
      <el-button type="primary" @click="openCreateEmployeeDialog">添加员工</el-button>
      <el-input
        v-model="data.name"
        clearable
        placeholder="员工姓名"
        @input="fetchEmployeeList"
      />
      <el-select v-model="data.flag" placeholder="状态" @change="fetchEmployeeList">
        <el-option label="正常" :value="0" />
        <el-option label="删除" :value="1" />
      </el-select>
      <el-button type="warning" plain class="toolbar-reset-btn" @click="resetEmployeeFilter">重置</el-button>
    </div>

    <el-table :data="data.tableData" stripe fit highlight-current-row empty-text="暂无数据">
      <el-table-column prop="id" label="ID" align="center" width="80" />
      <el-table-column prop="name" label="员工姓名" align="center" />
      <el-table-column prop="modeId" label="工作方式" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.modeId === 1" type="primary">压花</el-tag>
          <el-tag v-else-if="scope.row.modeId === 2" type="warning">刮胶</el-tag>
          <el-tag v-else type="info">未知</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdDate" label="创建日期" align="center" />
      <el-table-column prop="flag" label="状态" align="center" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.flag === 0" type="success">正常</el-tag>
          <el-tag v-else type="danger">删除</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180">
        <template #default="scope">
          <el-button
            text
            :disabled="scope.row.flag !== 0"
            @click="openEditEmployeeDialog(scope.row)"
          >
            编辑
          </el-button>
          <el-button v-if="scope.row.flag === 0" type="danger" text @click="removeEmployee(scope.row.id)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="employee-page">
      <el-pagination
        background
        :total="data.total"
        v-model:current-page="data.currentPage"
        v-model:page-size="data.pageSize"
        :page-sizes="[5, 10, 20, 50, 100]"
        layout="sizes, prev, pager, next, jumper, ->"
        @current-change="fetchEmployeeList"
        @size-change="fetchEmployeeList"
      />
    </div>

    <el-dialog
      v-model="data.editEmployeeDialogVisible"
      :title="data.editEmployeeDialogMode === 0 ? '添加员工' : '编辑员工'"
      width="40%"
      center
    >
      <el-form
        ref="employeeFormRef"
        :model="employeeFormRefModel"
        :rules="employeeFormRules"
        label-position="left"
        label-width="auto"
      >
        <el-form-item label="员工姓名" prop="name">
          <el-input v-model="employeeFormRefModel.name" placeholder="请输入员工姓名" />
        </el-form-item>
        <el-form-item label="工作方式" prop="modeId">
          <el-select v-model="employeeFormRefModel.modeId" placeholder="请选择工作方式">
            <el-option label="压花" :value="1" />
            <el-option label="刮胶" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeEmployeeDialog">取消</el-button>
        <el-button type="primary" @click="validateEmployeeForm(employeeFormRef)">
          {{ data.editEmployeeDialogMode === 0 ? '新增' : '编辑' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  deleteAdminEmployee,
  queryAdminEmployeeList,
  saveAdminEmployee,
  updateAdminEmployee
} from '../../../network/admin/index.js'
import {
  openCreateDialog,
  openEditDialog,
  resetReactiveForm,
  validateDialogForm
} from './factoryCrudShared.js'

onMounted(() => {
  fetchEmployeeList()
})

const data = reactive({
  name: '',
  flag: 0,
  tableData: [],
  total: 0,
  currentPage: 1,
  pageSize: 10,
  editEmployeeDialogVisible: false,
  editEmployeeDialogMode: 0
})

const fetchEmployeeList = async () => {
  const { data: res } = await queryAdminEmployeeList(
    (data.name || '').trim(),
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

  const list = res?.data || []
  data.tableData = list
  data.total = list.length
}

const createEmployee = async () => {
  const { data: res } = await saveAdminEmployee(employeeFormRefModel)
  if (res.status === 200) {
    ElMessage.success(res.message)
    closeEmployeeDialog()
    await fetchEmployeeList()
  } else {
    ElMessage.error(res.message)
  }
}

const updateEmployee = async () => {
  const { data: res } = await updateAdminEmployee(employeeFormRefModel)
  if (res.status === 200) {
    ElMessage.success(res.message)
    closeEmployeeDialog()
    await fetchEmployeeList()
  } else {
    ElMessage.error(res.message)
  }
}

const removeEmployee = async id => {
  const { data: res } = await deleteAdminEmployee(id)
  if (res.status === 200) {
    ElMessage.success(res.message)

    // 删除后若当前页无数据且不是第一页，回退一页再查
    if (data.tableData.length === 1 && data.currentPage > 1) {
      data.currentPage -= 1
    }

    await fetchEmployeeList()
  } else {
    ElMessage.error(res.message)
  }
}

const employeeFormRef = ref()
const employeeFormInit = { id: '', name: '', modeId: '' }
const employeeFormRefModel = reactive({ ...employeeFormInit })

const openCreateEmployeeDialog = () => {
  openCreateDialog(
    employeeFormRefModel,
    employeeFormInit,
    data,
    'editEmployeeDialogMode',
    'editEmployeeDialogVisible'
  )
}

const openEditEmployeeDialog = employee => {
  openEditDialog(
    employeeFormRefModel,
    employee,
    data,
    'editEmployeeDialogMode',
    'editEmployeeDialogVisible'
  )
}

const closeEmployeeDialog = () => {
  resetReactiveForm(employeeFormRefModel, employeeFormInit, employeeFormRef)
  data.editEmployeeDialogVisible = false
}

const validateEmployeeForm = async formEl => {
  await validateDialogForm(
    formEl,
    data.editEmployeeDialogMode,
    createEmployee,
    updateEmployee,
    () => ElMessage.error('请检查是否填写正确')
  )
}

const resetEmployeeFilter = () => {
  data.name = ''
  data.flag = 0
  data.currentPage = 1
  fetchEmployeeList()
}

const employeeFormRules = reactive({
  name: [{ required: true, message: '请输入员工姓名', trigger: 'blur' }],
  modeId: [{ required: true, message: '请选择工作方式', trigger: 'change' }]
})
</script>

<style scoped>
.employee-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.employee-toolbar {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  padding: 12px;
  gap: 12px;
  border-bottom: 1px solid var(--el-border-color);
  overflow-x: auto;
  overflow-y: hidden;
}

.employee-toolbar > *:nth-child(1) {
  width: 96px;
}

.employee-toolbar > *:nth-child(2) {
  width: 200px;
}

.employee-toolbar > *:nth-child(3) {
  width: 130px;
}

.employee-toolbar > *:nth-child(4) {
  width: 96px;
}

.employee-toolbar > * {
  flex-shrink: 0;
}

.employee-toolbar :deep(.el-input__wrapper),
.employee-toolbar :deep(.el-select__wrapper) {
  min-height: 32px;
}

.employee-page {
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  padding: 25px 0;
}

:deep(.el-table--fit) {
  flex: auto;
}

:deep(.employee-delete-popover .el-button) {
  color: var(--el-text-color-primary);
}

:deep(.employee-delete-popover .el-button--primary) {
  color: #fff;
}
</style>
