<template>
  <div class="page-container">
    <div class="toolbar">
      <el-button type="primary" @click="openCreate">添加工作类型</el-button>
      <el-input v-model="data.category" clearable placeholder="工作类型" @input="fetchList" />
      <el-select v-model="data.flag" @change="fetchList">
        <el-option label="正常" :value="0" />
        <el-option label="删除" :value="1" />
      </el-select>
      <el-button type="warning" plain class="toolbar-reset-btn" @click="resetFilter">重置</el-button>
    </div>

    <el-table :data="data.tableData" stripe fit>
      <el-table-column prop="id" label="ID" width="80" align="center" sortable />
      <el-table-column prop="category" label="工作类型" align="center" sortable />
      <el-table-column prop="createdDate" label="创建日期" align="center" sortable />
      <el-table-column prop="flag" label="状态" width="100" align="center" sortable>
        <template #default="scope">
          <el-tag v-if="scope.row.flag === 0" type="success">正常</el-tag>
          <el-tag v-else type="danger">删除</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center">
        <template #default="scope">
          <template v-if="isReadOnlyRow(scope.row)">
            <span class="readonly-text">已删除</span>
          </template>
          <el-button v-else type="danger" text @click="removeItem(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="page">
      <el-pagination
        background
        :total="data.total"
        v-model:current-page="data.currentPage"
        v-model:page-size="data.pageSize"
        :page-sizes="[5,10,20,50]"
        layout="sizes, prev, pager, next, jumper, ->"
        @current-change="fetchList"
        @size-change="fetchList"
      />
    </div>

    <el-dialog v-model="data.dialogVisible" title="添加工作类型" width="35%" center>
      <el-form ref="formRef" :model="formModel" :rules="rules" label-width="auto">
        <el-form-item label="工作类型" prop="category">
          <el-input v-model="formModel.category" />
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
import {
  saveJobCategory,
  deleteJobCategory,
  queryJobCategoryListCondition
} from '../../../network/admin/index.js'
import { openCreateDialog, resetReactiveForm, validateDialogForm } from './factoryCrudShared.js'

const data = reactive({ category: '', flag: 0, tableData: [], total: 0, currentPage: 1, pageSize: 10, dialogVisible: false })
const formRef = ref()
const formInit = { category: '' }
const formModel = reactive({ ...formInit })
const rules = reactive({ category: [{ required: true, message: '请输入工作类型', trigger: 'blur' }] })

onMounted(fetchList)

async function fetchList() {
  const { data: res } = await queryJobCategoryListCondition(data.currentPage, data.pageSize, data.category, data.flag)
  if (res.status === 200) {
    data.tableData = res?.data?.records || []
    data.total = res?.data?.total || 0
  } else ElMessage.error(res.message)
}

function openCreate() {
  openCreateDialog(formModel, formInit, data, null, 'dialogVisible')
}

function closeDialog() {
  resetReactiveForm(formModel, formInit, formRef)
  data.dialogVisible = false
}

function submit(formEl) {
  validateDialogForm(formEl, 0, async () => {
    const { data: res } = await saveJobCategory(formModel)
    if (res.status === 200) {
      ElMessage.success(res.message)
      closeDialog()
      await fetchList()
    } else ElMessage.error(res.message)
  }, null, () => ElMessage.error('请检查是否填写正确'))
}

async function removeItem(id) {
  const { data: res } = await deleteJobCategory(id)
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

<style scoped>
.page-container { height: 100%; display: flex; flex-direction: column; justify-content: space-between; }
.toolbar { display: flex; flex-wrap: nowrap; align-items: center; gap: 12px; padding: 12px; border-bottom: 1px solid var(--el-border-color); overflow-x: auto; overflow-y: hidden; }
.toolbar > *:nth-child(1) { width: 120px; }
.toolbar > *:nth-child(2) { width: 200px; }
.toolbar > *:nth-child(3) { width: 130px; }
.toolbar > *:nth-child(4) { width: 96px; }
.toolbar > * { flex-shrink: 0; }
.toolbar :deep(.el-input__wrapper), .toolbar :deep(.el-select__wrapper) { min-height: 32px; }
.page { display: flex; justify-content: center; padding: 25px 0; }
:deep(.el-table--fit) { flex: auto; }
.readonly-text { color: var(--el-text-color-placeholder); }
</style>
