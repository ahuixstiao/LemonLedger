<template>
  <div class="factory-job-category-container">
    <div class="factory-job-category-toolbar">
      <el-button type="primary" @click="openCreateFactoryJobCategoryDialog">添加</el-button>
      <el-input
        v-model="data.category"
        clearable
        placeholder="成衣厂工作类型"
        @input="filterFactoryJobCategoryList"
      />
      <el-select v-model="data.flag" placeholder="状态" @change="filterFactoryJobCategoryList">
        <el-option label="正常" :value="0" />
        <el-option label="删除" :value="1" />
      </el-select>
      <el-button type="warning" plain class="toolbar-reset-btn" @click="resetFactoryJobCategoryFilter">
        重置
      </el-button>
    </div>

    <el-table :data="data.tableData" stripe fit highlight-current-row empty-text="暂无数据">
      <el-table-column prop="id" label="ID" align="center" width="80" sortable />
      <el-table-column prop="category" label="成衣厂工作类型" align="center" sortable />
      <el-table-column prop="createdDate" label="创建日期" align="center" sortable />
      <el-table-column prop="flag" label="状态" align="center" width="100" sortable>
        <template #default="scope">
          <el-tag v-if="scope.row.flag === 0" type="success">正常</el-tag>
          <el-tag v-else type="danger">删除</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180">
        <template #default="scope">
          <template v-if="isReadOnlyRow(scope.row)">
            <span class="readonly-text">已删除</span>
          </template>
          <template v-else>
            <el-button text @click="openEditFactoryJobCategoryDialog(scope.row)">修改</el-button>
            <el-button type="danger" text @click="removeFactoryJobCategory(scope.row.id)">删除</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="data.dialogVisible" :title="data.dialogMode === 0 ? '添加成衣厂工作类型' : '修改成衣厂工作类型'" width="35%" center>
      <el-form ref="formRef" :model="factoryJobCategoryForm" :rules="rules" label-width="auto">
        <el-form-item label="成衣厂工作类型" prop="category">
          <el-input v-model="factoryJobCategoryForm.category" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeFactoryJobCategoryDialog">取消</el-button>
        <el-button type="primary" @click="submitFactoryJobCategory(formRef)">保存</el-button>
      </template>
    </el-dialog>

    <div class="factory-job-category-page">
      <el-pagination
        background
        :total="data.total"
        v-model:current-page="data.currentPage"
        v-model:page-size="data.pageSize"
        :page-sizes="[5, 10, 20, 50, 100]"
        layout="sizes, prev, pager, next, jumper, ->"
        @current-change="paginateFactoryJobCategoryList"
        @size-change="paginateFactoryJobCategoryList"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  deleteFactoryJobCategory,
  queryFactoryJobCategoryList,
  saveFactoryJobCategory,
  updateFactoryJobCategory
} from '../../../network/admin/index.js'
import { openCreateDialog, openEditDialog, resetReactiveForm, validateDialogForm } from './factoryCrudShared.js'

onMounted(() => {
  fetchFactoryJobCategoryList()
})

const data = reactive({
  category: '',
  flag: 0,
  sourceList: [],
  tableData: [],
  total: 0,
  currentPage: 1,
  pageSize: 10,
  dialogVisible: false,
  dialogMode: 0
})

const formRef = ref()
const factoryJobCategoryInit = { id: '', category: '' }
const factoryJobCategoryForm = reactive({ ...factoryJobCategoryInit })
const rules = reactive({ category: [{ required: true, message: '请输入成衣厂工作类型', trigger: 'blur' }] })

const filteredList = computed(() => {
  const keyword = data.category.trim().toLowerCase()

  return data.sourceList.filter(item => {
    const matchCategory = !keyword || String(item?.category || '').toLowerCase().includes(keyword)
    const matchFlag = Number(item?.flag) === Number(data.flag)
    return matchCategory && matchFlag
  })
})

/**
 * 查询成衣厂工作类型列表，并按前端筛选条件刷新表格分页数据。
 */
const fetchFactoryJobCategoryList = async () => {
  const { data: res } = await queryFactoryJobCategoryList()

  if (res.status !== 200) {
    data.sourceList = []
    data.tableData = []
    data.total = 0
    ElMessage.error(res.message)
    return
  }

  data.sourceList = Array.isArray(res?.data) ? res.data : []
  refreshFactoryJobCategoryPage()
}

/**
 * 根据当前筛选结果和分页参数生成表格展示数据。
 */
const refreshFactoryJobCategoryPage = () => {
  const list = filteredList.value
  const maxPage = Math.max(1, Math.ceil(list.length / data.pageSize))
  if (data.currentPage > maxPage) {
    data.currentPage = maxPage
  }

  const start = (data.currentPage - 1) * data.pageSize
  data.tableData = list.slice(start, start + data.pageSize)
  data.total = list.length
}

/**
 * 筛选条件变化时回到第一页并刷新表格数据。
 */
const filterFactoryJobCategoryList = () => {
  data.currentPage = 1
  refreshFactoryJobCategoryPage()
}

/**
 * 分页参数变化时刷新当前表格页。
 */
const paginateFactoryJobCategoryList = () => {
  refreshFactoryJobCategoryPage()
}

/**
 * 清空筛选条件并重新展示第一页数据。
 */
const resetFactoryJobCategoryFilter = () => {
  data.category = ''
  data.flag = 0
  data.currentPage = 1
  refreshFactoryJobCategoryPage()
}

/**
 * 打开新增弹窗。
 */
const openCreateFactoryJobCategoryDialog = () => {
  openCreateDialog(factoryJobCategoryForm, factoryJobCategoryInit, data, 'dialogMode', 'dialogVisible')
}

/**
 * 打开修改弹窗。
 *
 * @param {Object} row 当前行数据
 */
const openEditFactoryJobCategoryDialog = row => {
  openEditDialog(factoryJobCategoryForm, row, data, 'dialogMode', 'dialogVisible')
}

/**
 * 关闭弹窗并重置表单。
 */
const closeFactoryJobCategoryDialog = () => {
  resetReactiveForm(factoryJobCategoryForm, factoryJobCategoryInit, formRef)
  data.dialogVisible = false
}

/**
 * 保存当前表单数据。
 */
const submitFactoryJobCategory = formEl => {
  validateDialogForm(
    formEl,
    data.dialogMode,
    async () => {
      const { data: res } = await saveFactoryJobCategory(factoryJobCategoryForm)
      if (res.status === 200) {
        ElMessage.success(res.message)
        closeFactoryJobCategoryDialog()
        await fetchFactoryJobCategoryList()
      } else {
        ElMessage.error(res.message)
      }
    },
    async () => {
      const { data: res } = await updateFactoryJobCategory(factoryJobCategoryForm)
      if (res.status === 200) {
        ElMessage.success(res.message)
        closeFactoryJobCategoryDialog()
        await fetchFactoryJobCategoryList()
      } else {
        ElMessage.error(res.message)
      }
    },
    () => ElMessage.error('请检查是否填写正确')
  )
}

/**
 * 删除成衣厂工作类型。
 *
 * @param {number|string} id 记录ID
 */
const removeFactoryJobCategory = async id => {
  const { data: res } = await deleteFactoryJobCategory(id)
  if (res.status === 200) {
    ElMessage.success(res.message)
    await fetchFactoryJobCategoryList()
  } else {
    ElMessage.error(res.message)
  }
}

/**
 * 删除状态的数据仅允许展示，不提供操作入口。
 *
 * @param {Object} row 当前行数据
 */
const isReadOnlyRow = row => {
  return Number(row?.flag) === 1 || Number(data.flag) === 1
}
</script>

<style scoped>
.factory-job-category-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.factory-job-category-toolbar {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  padding: 12px;
  gap: 12px;
  border-bottom: 1px solid var(--el-border-color);
  overflow-x: auto;
  overflow-y: hidden;
}

.factory-job-category-toolbar > *:nth-child(1) {
  width: 96px;
}

.factory-job-category-toolbar > *:nth-child(2) {
  width: 220px;
}

.factory-job-category-toolbar > *:nth-child(3) {
  width: 130px;
}

.factory-job-category-toolbar > *:nth-child(4) {
  width: 96px;
}

.factory-job-category-toolbar > * {
  flex-shrink: 0;
}

.factory-job-category-toolbar :deep(.el-input__wrapper),
.factory-job-category-toolbar :deep(.el-select__wrapper) {
  min-height: 32px;
}

.factory-job-category-toolbar :deep(.el-button) {
  min-height: 32px;
}

.factory-job-category-page {
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  padding: 25px 0;
}

:deep(.el-table--fit) {
  flex: auto;
}
</style>
