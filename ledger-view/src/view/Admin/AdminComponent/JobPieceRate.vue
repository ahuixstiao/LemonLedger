<template>
  <div class="admin-page">
    <AdminPageHeader
      title="员工计件单价"
      desc="按「工作方式 + 工作类型」维护员工工价，工资核算直接取这份数据。改价只影响之后新建的账单。"
      :count="data.total"
      count-label="条单价"
    >
      <template #actions>
        <el-button type="primary" @click="openCreate">添加员工计件单价</el-button>
      </template>
    </AdminPageHeader>

    <div class="filter-card">
      <div class="filter-bar">
        <el-select v-model="data.modeId" clearable placeholder="工作方式" @change="onFilterChange">
          <el-option v-for="item in data.modeList" :key="item.id" :label="item.mode" :value="item.id" />
        </el-select>
        <el-select v-model="data.categoryId" clearable placeholder="工作类型" @change="onFilterChange">
          <el-option v-for="item in data.categoryList" :key="item.id" :label="item.category" :value="item.id" />
        </el-select>
        <el-select v-model="data.flag" placeholder="状态" @change="onFilterChange">
          <el-option label="正常" :value="0" />
          <el-option label="删除" :value="1" />
        </el-select>
        <div class="filter-actions">
          <el-button @click="resetFilter">重置</el-button>
        </div>
      </div>
    </div>

    <div class="table-card">
      <el-table height="100%" :data="data.tableData" stripe empty-text="还没有员工计件单价">
        <el-table-column prop="id" label="ID" width="72" align="center" sortable />
        <el-table-column prop="mode" label="工作方式" min-width="140" sortable>
          <template #default="scope">
            <el-tag :type="modeTagType(scope.row.modeId)" effect="light">{{ scope.row.mode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="工作类型" min-width="160" sortable />
        <el-table-column prop="price" label="计件单价" align="right" width="140" sortable>
          <template #default="scope">
            <span class="money">{{ formatMoney(scope.row.price) }}</span>
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
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="scope">
            <span v-if="isReadOnlyRow(scope.row)" class="readonly-text">已删除，不可操作</span>
            <template v-else>
              <el-button text type="primary" @click="openEdit(scope.row)">编辑</el-button>
              <el-button text type="danger" @click="removeItem(scope.row)">删除</el-button>
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
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchList"
        @size-change="onSizeChange"
      />
    </div>

    <el-dialog
      v-model="data.dialogVisible"
      :title="data.dialogMode === 0 ? '添加员工计件单价' : '编辑员工计件单价'"
      width="420px"
    >
      <el-form ref="formRef" :model="formModel" :rules="rules" label-position="top">
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
        <el-form-item label="计件单价（元 / 件）" prop="price">
          <el-input v-model="formModel.price" placeholder="请输入计件单价">
            <template #prefix>¥</template>
          </el-input>
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
import AdminPageHeader from '../AdminPageHeader.vue'
import { confirmDelete, formatDate, formatMoney, modeTagType } from '../adminShared.js'
import {
  queryJobPieceRateList,
  saveJobPieceRate,
  updateJobPieceRate,
  deleteJobPieceRate,
  queryJobModeList,
  queryJobCategoryListCondition
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
const formInit = { id: '', modeId: '', categoryId: '', price: '' }
const formModel = reactive({ ...formInit })
const rules = reactive({
  modeId: [{ required: true, message: '请选择工作方式', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择工作类型', trigger: 'change' }],
  price: [{ required: true, message: '请输入计件单价', trigger: 'blur' }]
})

const OPTION_QUERY = { currentPage: 1, pageSize: 1000, keyword: '', flag: 0 }

onMounted(async () => {
  await Promise.all([loadModeList(), loadCategoryList()])
  fetchList()
})

async function loadModeList() {
  const { data: res } = await queryJobModeList(
    OPTION_QUERY.currentPage,
    OPTION_QUERY.pageSize,
    OPTION_QUERY.keyword,
    OPTION_QUERY.flag
  )
  data.modeList = res?.data?.records || []
}

async function loadCategoryList() {
  const { data: res } = await queryJobCategoryListCondition(
    OPTION_QUERY.currentPage,
    OPTION_QUERY.pageSize,
    OPTION_QUERY.keyword,
    OPTION_QUERY.flag
  )
  data.categoryList = res?.data?.records || []
}

async function fetchList() {
  const { data: res } = await queryJobPieceRateList(
    data.modeId,
    data.categoryId,
    data.currentPage,
    data.pageSize,
    data.flag
  )
  if (res.status === 200) {
    data.tableData = res?.data?.records || []
    data.total = res?.data?.total || 0
  } else ElMessage.error(res.message)
}

function onFilterChange() {
  data.currentPage = 1
  fetchList()
}

function onSizeChange() {
  data.currentPage = 1
  fetchList()
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
      const { data: res } = await saveJobPieceRate(formModel)
      if (res.status === 200) {
        ElMessage.success(res.message)
        closeDialog()
        await fetchList()
      } else ElMessage.error(res.message)
    },
    async () => {
      const { data: res } = await updateJobPieceRate(formModel)
      if (res.status === 200) {
        ElMessage.success(res.message)
        closeDialog()
        await fetchList()
      } else ElMessage.error(res.message)
    },
    () => ElMessage.error('请检查是否填写正确')
  )
}

async function removeItem(row) {
  const confirmed = await confirmDelete(
    `确定删除「${row.mode} · ${row.category}」这条单价吗？`
  )
  if (!confirmed) return

  const { data: res } = await deleteJobPieceRate(row.id)
  if (res.status === 200) {
    ElMessage.success(res.message)
    await fetchList()
  } else ElMessage.error(res.message)
}

function resetFilter() {
  data.modeId = ''
  data.categoryId = ''
  data.flag = 0
  data.currentPage = 1
  fetchList()
}

/**
 * 删除状态的数据仅允许展示，不提供编辑/删除入口。
 */
function isReadOnlyRow(row) {
  return Number(row?.flag) === 1 || Number(data.flag) === 1
}
</script>
