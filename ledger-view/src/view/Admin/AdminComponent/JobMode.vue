<template>
  <div class="page-container">
    <div class="toolbar">
      <el-button type="primary" @click="openCreate">添加工作方式</el-button>
      <el-input v-model="data.mode" clearable placeholder="工作方式" @input="fetchList" />
    </div>

    <el-table :data="data.tableData" stripe fit>
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="mode" label="工作方式" align="center" />
      <el-table-column prop="createdDate" label="创建日期" align="center" />
      <el-table-column label="操作" width="120" align="center">
        <template #default="scope">
          <el-popconfirm title="确认删除?" @confirm="removeItem(scope.row.id)">
            <template #reference><el-button type="danger" text>删除</el-button></template>
          </el-popconfirm>
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

    <el-dialog v-model="data.dialogVisible" title="添加工作方式" width="35%" center>
      <el-form ref="formRef" :model="formModel" :rules="rules" label-width="auto">
        <el-form-item label="工作方式" prop="mode">
          <el-input v-model="formModel.mode" />
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
import { queryJobModeList, saveJobMode, deleteJobMode } from '../../../network/admin/index.js'
import { openCreateDialog, resetReactiveForm, validateDialogForm } from './factoryCrudShared.js'

const data = reactive({ mode: '', tableData: [], total: 0, currentPage: 1, pageSize: 10, dialogVisible: false })
const formRef = ref()
const formInit = { mode: '' }
const formModel = reactive({ ...formInit })
const rules = reactive({ mode: [{ required: true, message: '请输入工作方式', trigger: 'blur' }] })

onMounted(fetchList)

async function fetchList() {
  const { data: res } = await queryJobModeList(data.currentPage, data.pageSize, data.mode)
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
    const { data: res } = await saveJobMode(formModel)
    if (res.status === 200) {
      ElMessage.success(res.message)
      closeDialog()
      await fetchList()
    } else ElMessage.error(res.message)
  }, null, () => ElMessage.error('请检查是否填写正确'))
}

async function removeItem(id) {
  const { data: res } = await deleteJobMode(id)
  if (res.status === 200) {
    ElMessage.success(res.message)
    await fetchList()
  } else ElMessage.error(res.message)
}
</script>

<style scoped>
.page-container { height: 100%; display: flex; flex-direction: column; justify-content: space-between; }
.toolbar { display: flex; gap: 10px; padding: 10px; border-bottom: 1px solid var(--el-border-color); }
.page { display: flex; justify-content: center; padding: 25px 0; }
:deep(.el-table--fit) { flex: auto; }
</style>
