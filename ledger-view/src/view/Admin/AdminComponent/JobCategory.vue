<template>
  <div class="admin-page">
    <AdminPageHeader
      title="工作类型"
      desc="工作类型是账单、单价和工单共用的字典。新增后会自动出现在各个下拉框里，建议保持命名简短一致。"
      :count="data.total"
      count-label="个工作类型"
    >
      <template #actions>
        <el-button type="primary" @click="openCreate">添加工作类型</el-button>
      </template>
    </AdminPageHeader>

    <div class="filter-card">
      <div class="filter-bar">
        <el-input v-model="data.category" clearable placeholder="搜索工作类型" @input="onSearch">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="data.flag" placeholder="状态" @change="onStatusChange">
          <el-option label="正常" :value="0" />
          <el-option label="删除" :value="1" />
        </el-select>
        <div class="filter-actions">
          <el-button @click="resetFilter">重置</el-button>
        </div>
      </div>
    </div>

    <div class="table-card">
      <el-table height="100%" :data="data.tableData" stripe empty-text="还没有工作类型，先添加一个">
        <el-table-column prop="id" label="ID" sortable min-width="90" align="center" />
        <el-table-column prop="category" label="工作类型" sortable min-width="340" align="center">
          <template #default="scope">
            <div class="entity-cell">
              <span class="entity-avatar">{{ (scope.row.category || '?').slice(0, 1) }}</span>
              <span class="entity-name">{{ scope.row.category }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createdDate" label="创建日期" sortable min-width="170" align="center">
          <template #default="scope">
            <span class="date-cell">{{ formatDate(scope.row.createdDate) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="flag" label="状态" sortable min-width="120" align="center">
          <template #default="scope">
            <span
              class="status-pill"
              :class="scope.row.flag === 0 ? 'is-active' : 'is-removed'"
            >{{ scope.row.flag === 0 ? '正常' : '删除' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="180" align="center">
          <template #default="scope">
            <span v-if="isReadOnlyRow(scope.row)" class="readonly-text">已删除，不可操作</span>
            <el-button v-else text type="danger" @click="removeItem(scope.row)">删除</el-button>
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
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchList"
        @size-change="onSizeChange"
      />
    </div>

    <el-dialog v-model="data.dialogVisible" title="添加工作类型" width="400px">
      <el-form ref="formRef" :model="formModel" :rules="rules" label-position="top">
        <el-form-item label="工作类型" prop="category">
          <el-input v-model="formModel.category" placeholder="例如：压花、刮胶、包装" maxlength="30" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="submit(formRef)">保存</el-button>
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
  saveJobCategory,
  deleteJobCategory,
  queryJobCategoryListCondition
} from '../../../network/admin/index.js'
import { openCreateDialog, resetReactiveForm, validateDialogForm } from './factoryCrudShared.js'

const data = reactive({
  category: '',
  flag: 0,
  tableData: [],
  total: 0,
  currentPage: 1,
  pageSize: 10,
  dialogVisible: false
})
const formRef = ref()
const formInit = { category: '' }
const formModel = reactive({ ...formInit })
const rules = reactive({ category: [{ required: true, message: '请输入工作类型', trigger: 'blur' }] })

onMounted(fetchList)

async function fetchList() {
  const { data: res } = await queryJobCategoryListCondition(
    data.currentPage,
    data.pageSize,
    data.category,
    data.flag
  )
  if (res.status === 200) {
    data.tableData = res?.data?.records || []
    data.total = res?.data?.total || 0
  } else ElMessage.error(res.message)
}

function onSearch() {
  data.currentPage = 1
  fetchList()
}

function onStatusChange() {
  data.currentPage = 1
  fetchList()
}

function onSizeChange() {
  data.currentPage = 1
  fetchList()
}

function openCreate() {
  openCreateDialog(formModel, formInit, data, null, 'dialogVisible')
}

function closeDialog() {
  resetReactiveForm(formModel, formInit, formRef)
  data.dialogVisible = false
}

function submit(formEl) {
  validateDialogForm(
    formEl,
    0,
    async () => {
      const { data: res } = await saveJobCategory(formModel)
      if (res.status === 200) {
        ElMessage.success(res.message)
        closeDialog()
        await fetchList()
      } else ElMessage.error(res.message)
    },
    null,
    () => ElMessage.error('请检查是否填写正确')
  )
}

async function removeItem(row) {
  const confirmed = await confirmDelete(`确定删除工作类型「${row.category}」吗？`)
  if (!confirmed) return

  const { data: res } = await deleteJobCategory(row.id)
  if (res.status === 200) {
    ElMessage.success(res.message)
    await fetchList()
  } else ElMessage.error(res.message)
}

function resetFilter() {
  data.category = ''
  data.flag = 0
  data.currentPage = 1
  fetchList()
}

/**
 * 删除状态的数据仅允许展示，不提供操作入口。
 */
function isReadOnlyRow(row) {
  return Number(row?.flag) === 1 || Number(data.flag) === 1
}
</script>
