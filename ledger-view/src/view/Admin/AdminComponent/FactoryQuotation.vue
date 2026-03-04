<template>
  <div class="factoryQuotation-container">
    <!-- 筛选条件 -->
    <div class="factoryQuotation-button">
      <el-button type="primary" @click="openCreateQuotationDialog">添加</el-button>

      <!-- 成衣厂筛选条件 -->
      <el-select
          v-model="data.factoryId"
          filterable
          clearable
          placeholder="选择工厂名称"
          @change="fetchFactoryQuotationList"
      >
        <el-option
            v-for="item in data.factoryList"
            :label="item.factoryName"
            :value="item.id"
        />
      </el-select>

      <!-- 款式编号筛选条件 -->
      <el-input
          clearable
          v-model="data.styleNumber"
          placeholder="款式编号"
          @input="fetchFactoryQuotationList"
      >
      </el-input>

      <!-- 工作类型筛选条件 -->
      <el-select
          v-model="data.categoryId"
          clearable
          placeholder="选择工作类型"
          @change="fetchFactoryQuotationList"
      >
        <el-option
            v-for="item in data.categoryList"
            :label="item.category"
            :value="item.id"
        />
      </el-select>

      <!-- 状态筛选条件 -->
      <el-select
          v-model="data.flag"
          placeholder="状态"
          @change="fetchFactoryQuotationList"
      >
        <el-option label="正常" :value="0" />
        <el-option label="删除" :value="1" />
      </el-select>

      <el-button type="warning" plain class="toolbar-reset-btn" @click="resetQuotationFilter">重置</el-button>
    </div>

    <!-- 成衣厂报价表格 -->
    <el-table
        :data="data.tableData"
        style="width: 100%"
        stripe
        fit
        highlight-current-row
        empty-text="暂无数据"
    >
      <el-table-column prop="factoryName" sortable label="厂名" align="center"/>
      <el-table-column prop="styleNumber" sortable label="款式编号" align="center"/>
      <el-table-column prop="category" sortable label="工作类型" align="center"/>
      <el-table-column prop="quotation" sortable label="报价 (元)" align="center"/>
      <el-table-column prop="createdDate" sortable label="创建日期" align="center"/>
      <el-table-column prop="flag" sortable label="状态" align="center" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.flag === 0" type="success">正常</el-tag>
          <el-tag v-else type="danger">删除</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160px">
        <template #default="scope">
          <template v-if="isReadOnlyRow(scope.row)">
            <span class="readonly-text">已删除</span>
          </template>
          <template v-else>
            <el-button text @click="openEditQuotationDialog(scope.row)">编辑</el-button>
            <el-button type="danger" text @click="removeFactoryQuotation(scope.row.id)">删除</el-button>
          </template>
        </template>
      </el-table-column>

    </el-table>

    <!-- 分页组件 -->
    <div class="factoryQuotation-page">
      <el-pagination
          background
          :total="data.total"
          v-model:current-page="data.currentPage"
          v-model:page-size="data.pageSize"
          :page-sizes="[5, 10, 20, 50, 100]"
          layout="sizes, prev, pager, next, jumper, ->"
          @current-change="fetchFactoryQuotationList"
          @size-change="fetchFactoryQuotationList"
      />
    </div>

    <!--  添加或编辑成衣厂账单信息弹窗  -->
    <el-dialog
        v-model="data.editQuotationDialogVisible"
        :title="
        data.editQuotationDialogMode === 0 ? '添加成衣厂报价单' : data.editQuotationDialogMode === 1 ? '修改成衣厂报价单': ''"
        width="50%"
        center
    >
      <el-form
          ref="quotationInfoFormRef"
          :model="factoryQuotationInfoRef"
          :rules="addQuotationInfoRules"
          label-width="auto"
          label-position="left"
      >
        <el-form-item size="large" label="工厂名称:" prop="factoryId">
          <el-select
              filterable
              v-model="factoryQuotationInfoRef.factoryId"
              placeholder="选择工厂名称"
          >
            <el-option
                v-for="item in data.factoryList"
                :label="item.factoryName"
                :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item size="large" label="款式编号:" prop="styleNumber">
          <el-input
              v-model="factoryQuotationInfoRef.styleNumber"
              placeholder="输入款式编号"
          />
        </el-form-item>

        <el-form-item size="large" label="工作类型:" prop="categoryId">
          <el-select
              v-model="factoryQuotationInfoRef.categoryId"
              placeholder="选择工作类型"
          >
            <el-option
                v-for="item in data.categoryList"
                :label="item.category"
                :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item size="large" label="报价:" prop="quotation">
          <el-input
              v-model="factoryQuotationInfoRef.quotation"
              placeholder="输入报价"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeQuotationDialog">取消</el-button>
          <el-button type="primary" v-if="data.editQuotationDialogMode === 0"
                     @click="validateQuotationForm(quotationInfoFormRef)">
            新增
          </el-button>
          <el-button type="primary"
                     v-if="data.editQuotationDialogMode === 1" @click="validateQuotationForm(quotationInfoFormRef)">
            修改
          </el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>


<script setup>

import { onMounted, reactive, ref } from 'vue'
import { queryFactoryList } from '../../../network/index.js'
import {
  deleteFactoryQuotationInfo,
  queryFactoryJobCategoryList,
  queryFactoryQuotationList,
  saveFactoryQuotationInfo,
  updateFactoryQuotationInfo
} from '../../../network/admin/index.js'
import { ElMessage } from 'element-plus'
import {
  loadFactoryOptions,
  loadFactoryCategoryOptions,
  openCreateDialog,
  openEditDialog,
  resetReactiveForm,
  validateDialogForm
} from './factoryCrudShared.js'


onMounted(() => {
  loadFactoryList()
  loadCategoryList()
  fetchFactoryQuotationList()
})

const data = reactive({
  // 表格数据
  tableData: [],

  factoryList: [],
  categoryList: [],

  factoryId: '',
  number: '',
  styleNumber: '',
  categoryId: '',
  flag: 0,
  currentPage: 1,
  pageSize: 10,
  total: 0,

  editQuotationDialogVisible: false,
  editQuotationDialogMode: 0,
})

// 加载成衣厂选项
const loadFactoryList = async () => {
  await loadFactoryOptions(data, queryFactoryList)
}

// 加载工作类型选项
const loadCategoryList = async () => {
  await loadFactoryCategoryOptions(data, queryFactoryJobCategoryList)
}

// TODO 查询成衣厂报价单列表
const fetchFactoryQuotationList = async () => {

  const {data: res} = await queryFactoryQuotationList(
      data.factoryId,
      data.styleNumber,
      data.categoryId,
      data.currentPage,
      data.pageSize,
      data.flag
  )
  // 判断请求是否成功
  if (res.status === 200) {
    data.tableData = res.data.records
    data.total = res.data.total
  } else {
    data.tableData = []
  }

}

const resetQuotationFilter = () => {
  data.factoryId = ''
  data.styleNumber = ''
  data.categoryId = ''
  data.flag = 0
  data.currentPage = 1
  fetchFactoryQuotationList()
}

/**
 * 删除状态的数据仅允许展示，不提供编辑/删除入口。
 */
const isReadOnlyRow = row => {
  return Number(row?.flag) === 1 || Number(data.flag) === 1
}

const createFactoryQuotation = async () => {
  const { data: res } = await saveFactoryQuotationInfo(factoryQuotationInfoRef)
  if (res.status === 200) {
    ElMessage.success(res.message)
    await fetchFactoryQuotationList()
  } else {
    ElMessage.error(res.message)
  }
}

const updateFactoryQuotation = async () => {
  const { data: res } = await updateFactoryQuotationInfo(factoryQuotationInfoRef)
  if (res.status === 200) {
    ElMessage.success(res.message)
    await fetchFactoryQuotationList()
  } else {
    ElMessage.error(res.message)
  }
  data.editQuotationDialogVisible = false
}

const removeFactoryQuotation = async id => {
  const { data: res } = await deleteFactoryQuotationInfo(id)
  if (res.status === 200) {
    ElMessage.success(res.message)
    await fetchFactoryQuotationList()
  } else {
    ElMessage.error(res.message)
  }
}

const validateQuotationForm = async formEl => {
  await validateDialogForm(
    formEl,
    data.editQuotationDialogMode,
    createFactoryQuotation,
    updateFactoryQuotation,
    () => ElMessage.error('请检查是否填写正确')
  )
}

const quotationInfoFormRef = ref()

// 成衣厂报价单实体初始化
const factoryQuotationInfoInit = {
  id: '',
  factoryId: '',
  styleNumber: '',
  categoryId: '',
  quotation: ''
}

// 构建报价单实体
const factoryQuotationInfoRef = reactive({...factoryQuotationInfoInit})


const openCreateQuotationDialog = () => {
  openCreateDialog(
    factoryQuotationInfoRef,
    factoryQuotationInfoInit,
    data,
    'editQuotationDialogMode',
    'editQuotationDialogVisible'
  )
}

const openEditQuotationDialog = quotationInfo => {
  openEditDialog(
    factoryQuotationInfoRef,
    quotationInfo,
    data,
    'editQuotationDialogMode',
    'editQuotationDialogVisible'
  )
}

const resetQuotationForm = () => {
  resetReactiveForm(factoryQuotationInfoRef, factoryQuotationInfoInit, quotationInfoFormRef)
}

const closeQuotationDialog = () => {
  resetQuotationForm()
  data.editQuotationDialogVisible = false
}

// 添加账单记录校验规则
const addQuotationInfoRules = reactive({
  factoryId: [{required: true, message: '请选择厂名', trigger: 'blur'}],
  styleNumber: [{required: true, message: '请输入款式编号', trigger: 'blur'}],
  categoryId: [{required: true, message: '请选择工作类型', trigger: 'blur'}],
  quotation: [{required: true, message: '请输入报价', trigger: 'blur'}],
})

</script>

<style scoped>
.factoryQuotation-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.factoryQuotation-button {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  padding: 12px;
  gap: 12px;
  border-bottom: 1px solid var(--el-border-color);
  overflow-x: auto;
  overflow-y: hidden;
}

.factoryQuotation-button > *:nth-child(1) {
  width: 96px;
}

.factoryQuotation-button > *:nth-child(2) {
  width: 200px;
}

.factoryQuotation-button > *:nth-child(3) {
  width: 180px;
}

.factoryQuotation-button > *:nth-child(4) {
  width: 160px;
}

.factoryQuotation-button > *:nth-child(5) {
  width: 110px;
}

.factoryQuotation-button > *:nth-child(6) {
  width: 96px;
}

.factoryQuotation-button > * {
  flex-shrink: 0;
}

.factoryQuotation-button :deep(.el-input__wrapper),
.factoryQuotation-button :deep(.el-select__wrapper) {
  min-height: 32px;
}

.factoryQuotation-page {
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  padding: 25px 0;
}

:deep(.el-form-item__content) {
  flex-wrap: nowrap;
  gap: 10px;
}

:deep(.el-table--fit) {
  flex: auto;
}

.readonly-text {
  color: var(--el-text-color-placeholder);
}

</style>