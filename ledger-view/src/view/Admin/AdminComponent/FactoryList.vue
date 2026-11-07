<template>
  <div class="admin-page">
    <AdminPageHeader
      title="成衣厂列表"
      desc="维护合作成衣厂的基础信息。账单、计件单价和毛利统计都按这里的厂名归集。"
      :count="data.total"
      count-label="家工厂"
    >
      <template #actions>
        <el-button type="primary" @click="openCreateFactoryDialog">添加工厂</el-button>
      </template>
    </AdminPageHeader>

    <div class="filter-card">
      <div class="filter-bar">
        <el-input
          v-model="data.factoryName"
          clearable
          placeholder="搜索工厂名称"
          @input="fetchFactoryList"
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
          <el-button @click="resetFactoryFilter">重置</el-button>
        </div>
      </div>
    </div>

    <div class="table-card">
      <el-table height="100%" :data="data.tableData" stripe highlight-current-row empty-text="没有找到成衣厂">
        <el-table-column prop="id" label="ID" align="center" width="72" sortable />
        <el-table-column label="工厂名称" min-width="200" sortable prop="factoryName">
          <template #default="scope">
            <div class="entity-cell">
              <span class="entity-avatar">{{ (scope.row.factoryName || '?').slice(0, 1) }}</span>
              <span class="entity-name">{{ scope.row.factoryName }}</span>
            </div>
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
            <span v-if="isReadOnlyRow(scope.row)" class="readonly-text">已删除，不可编辑</span>
            <template v-else>
              <el-button text type="primary" @click="openEditFactoryDialog(scope.row)">编辑</el-button>
              <el-button text type="danger" @click="removeFactory(scope.row)">删除</el-button>
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
        @current-change="fetchFactoryList"
        @size-change="onSizeChange"
      />
    </div>

    <el-dialog
      v-model="data.editFactoryDialogVisible"
      :title="data.editFactoryDialogMode === 0 ? '添加工厂' : '修改工厂'"
      width="420px"
    >
      <el-form
        ref="factoryFormRef"
        :model="factoryFormRefModel"
        :rules="factoryFormRules"
        label-position="top"
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
            class="filter-date"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeFactoryDialog">取消</el-button>
        <el-button type="primary" @click="validateFactoryForm(factoryFormRef)">
          {{ data.editFactoryDialogMode === 0 ? '新增' : '保存' }}
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

onMounted(() => {
  fetchFactoryList()
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

const onStatusChange = () => {
  data.currentPage = 1
  fetchFactoryList()
}

const onSizeChange = () => {
  data.currentPage = 1
  fetchFactoryList()
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

const removeFactory = async factory => {
  const confirmed = await confirmDelete(
    `确定删除成衣厂「${factory.factoryName}」吗？删除后账单记录不会被清掉，但不能再选了。`
  )
  if (!confirmed) return

  const { data: res } = await deleteAdminFactory(factory.id)
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
