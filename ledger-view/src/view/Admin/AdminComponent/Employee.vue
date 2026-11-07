<template>
  <div class="admin-page">
    <AdminPageHeader
      title="员工档案"
      desc="维护员工姓名与工作方式。删除只是把记录标记为「删除」，切到对应状态还能找回。"
      :count="data.total"
      count-label="名员工"
    >
      <template #actions>
        <el-button type="primary" @click="openCreateEmployeeDialog">添加员工</el-button>
      </template>
    </AdminPageHeader>

    <div class="filter-card">
      <div class="filter-bar">
        <el-input
          v-model="data.name"
          clearable
          placeholder="搜索员工姓名"
          @input="fetchEmployeeList"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="data.flag" placeholder="状态" @change="onStatusChange">
          <el-option label="正常" :value="0" />
          <el-option label="删除" :value="1" />
        </el-select>
        <div class="filter-actions">
          <el-button @click="resetEmployeeFilter">重置</el-button>
        </div>
      </div>
    </div>

    <div class="table-card">
      <el-table height="100%" :data="data.tableData" stripe highlight-current-row empty-text="没有找到员工记录">
        <el-table-column prop="id" label="ID" align="center" width="72" sortable />
        <el-table-column label="员工" min-width="160" sortable prop="name">
          <template #default="scope">
            <div class="entity-cell">
              <span class="entity-avatar">{{ (scope.row.name || '?').slice(0, 1) }}</span>
              <span class="entity-name">{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="modeId" label="工作方式" align="center" width="120" sortable>
          <template #default="scope">
            <el-tag v-if="scope.row.modeId === 1" type="primary">压花</el-tag>
            <el-tag v-else-if="scope.row.modeId === 2" type="warning">刮胶</el-tag>
            <el-tag v-else type="info">未知</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdDate" label="创建日期" align="center" width="140" sortable>
          <template #default="scope">
            <span class="date-cell">{{ formatDate(scope.row.createdDate) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="flag" label="状态" align="center" width="110" sortable>
          <template #default="scope">
            <span
              class="status-pill"
              :class="scope.row.flag === 0 ? 'is-active' : 'is-removed'"
            >{{ scope.row.flag === 0 ? '正常' : '删除' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="160" fixed="right">
          <template #default="scope">
            <span v-if="scope.row.flag !== 0" class="readonly-text">已删除，不可操作</span>
            <template v-else>
              <el-button text type="primary" @click="openEditEmployeeDialog(scope.row)">编辑</el-button>
              <el-button text type="danger" @click="removeEmployee(scope.row)">删除</el-button>
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
        @current-change="fetchEmployeeList"
        @size-change="onSizeChange"
      />
    </div>

    <el-dialog
      v-model="data.editEmployeeDialogVisible"
      :title="data.editEmployeeDialogMode === 0 ? '添加员工' : '编辑员工'"
      width="420px"
    >
      <el-form
        ref="employeeFormRef"
        :model="employeeFormRefModel"
        :rules="employeeFormRules"
        label-position="top"
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
          {{ data.editEmployeeDialogMode === 0 ? '新增' : '保存' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import AdminPageHeader from '../AdminPageHeader.vue'
import { confirmDelete, formatDate } from '../adminShared.js'
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

onMounted(() => {
  fetchEmployeeList()
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

const onStatusChange = () => {
  data.currentPage = 1
  fetchEmployeeList()
}

const onSizeChange = () => {
  data.currentPage = 1
  fetchEmployeeList()
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

const removeEmployee = async employee => {
  const confirmed = await confirmDelete(`确定删除员工「${employee.name}」吗？删除后可在「删除」状态里找回。`)
  if (!confirmed) return

  const { data: res } = await deleteAdminEmployee(employee.id)
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
