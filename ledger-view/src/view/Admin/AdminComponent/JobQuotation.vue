<template>
  <div class="page-container">
    <div class="toolbar">
      <el-button type="primary" @click="openCreate">添加工作报价</el-button>
      <el-select v-model="data.modeId" clearable placeholder="工作方式" @change="fetchList">
        <el-option v-for="item in data.modeList" :key="item.id" :label="item.mode" :value="item.id" />
      </el-select>
      <el-select v-model="data.categoryId" clearable placeholder="工作类型" @change="fetchList">
        <el-option v-for="item in data.categoryList" :key="item.id" :label="item.category" :value="item.id" />
      </el-select>
      <el-select v-model="data.flag" @change="fetchList">
        <el-option label="正常" :value="0" />
        <el-option label="删除" :value="1" />
      </el-select>
    </div>

    <el-table :data="data.tableData" stripe fit>
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="modeId" label="工作方式" align="center">
        <template #default="scope">{{ modeText(scope.row.modeId) }}</template>
      </el-table-column>
      <el-table-column prop="categoryId" label="工作类型" align="center">
        <template #default="scope">{{ categoryText(scope.row.categoryId) }}</template>
      </el-table-column>
      <el-table-column prop="quotation" label="报价" align="center" />
      <el-table-column prop="createdDate" label="创建日期" align="center" />
      <el-table-column label="操作" width="160" align="center">
        <template #default="scope">
          <el-button text @click="openEdit(scope.row)">编辑</el-button>
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

    <el-dialog v-model="data.dialogVisible" :title="data.dialogMode === 0 ? '添加工作报价' : '编辑工作报价'" width="40%" center>
      <el-form ref="formRef" :model="formModel" :rules="rules" label-width="auto">
        <el-form-item label="工作方式" prop="modeId">
          <el-select v-model="formModel.modeId" placeholder="请选择工作方式">
            <el-option v-for="item in data.modeList" :key="item.id" :label="item.mode" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="工作类型" prop="categoryId">
          <el-select v-model="formModel.categoryId" placeholder="请选择工作类型">
            <el-option v-for="item in data.categoryList" :key="item.id" :label="item.category" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="报价" prop="quotation">
          <el-input v-model="formModel.quotation" placeholder="请输入报价" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="submit(formRef)">{{ data.dialogMode === 0 ? '保存' : '更新' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  queryJobQuotationList,
  saveJobQuotation,
  updateJobQuotation,
  deleteJobQuotation,
  queryJobModeList,
  queryJobCategoryList
} from '../../../network/admin/index.js'
import { openCreateDialog, openEditDialog, resetReactiveForm, validateDialogForm } from './factoryCrudShared.js'

const data = reactive({
  modeId: '',
  categoryId: '',
  flag: 0,
  tableData: [],
  total: 0,
  currentPage: 1,
  pageSize: 10,
  modeList: [],
  categoryList: [],
  dialogVisible: false,
  dialogMode: 0
})
const formRef = ref()
const formInit = { id: '', modeId: '', categoryId: '', quotation: '' }
const formModel = reactive({ ...formInit })
const rules = reactive({
  modeId: [{ required: true, message: '请选择工作方式', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择工作类型', trigger: 'change' }],
  quotation: [{ required: true, message: '请输入报价', trigger: 'blur' }]
})

onMounted(async () => {
  await Promise.all([loadModeList(), loadCategoryList()])
  fetchList()
})

async function loadModeList() {
  const { data: res } = await queryJobModeList(1, 1000, '')
  data.modeList = res?.data?.records || []
}

async function loadCategoryList() {
  const { data: res } = await queryJobCategoryList('', 1, 1000, 0)
  data.categoryList = res?.data?.records || []
}

async function fetchList() {
  const { data: res } = await queryJobQuotationList(data.modeId, data.categoryId, data.currentPage, data.pageSize, data.flag)
  if (res.status === 200) {
    data.tableData = res?.data?.records || []
    data.total = res?.data?.total || 0
  } else ElMessage.error(res.message)
}

function modeText(modeId) {
  return data.modeList.find(item => item.id === modeId)?.mode || '-'
}

function categoryText(categoryId) {
  return data.categoryList.find(item => item.id === categoryId)?.category || '-'
}

function openCreate() {
  openCreateDialog(formModel, formInit, data, 'dialogMode', 'dialogVisible')
}

function openEdit(row) {
  openEditDialog(formModel, row, data, 'dialogMode', 'dialogVisible')
}

function closeDialog() {
  resetReactiveForm(formModel, formInit, formRef)
  data.dialogVisible = false
}

function submit(formEl) {
  validateDialogForm(
    formEl,
    data.dialogMode,
    async () => {
      const { data: res } = await saveJobQuotation(formModel)
      if (res.status === 200) {
        ElMessage.success(res.message)
        closeDialog()
        await fetchList()
      } else ElMessage.error(res.message)
    },
    async () => {
      const { data: res } = await updateJobQuotation(formModel)
      if (res.status === 200) {
        ElMessage.success(res.message)
        closeDialog()
        await fetchList()
      } else ElMessage.error(res.message)
    },
    () => ElMessage.error('请检查是否填写正确')
  )
}

async function removeItem(id) {
  const { data: res } = await deleteJobQuotation(id)
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
