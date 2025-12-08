<template>
  <div class="factoryQuotation-container">
    <!-- 筛选条件 -->
    <div class="factoryQuotation-button">
      <el-button type="primary" @click="clickAddButton">添加</el-button>

      <!-- 成衣厂筛选条件 -->
      <el-select
          v-model="data.factoryId"
          filterable
          clearable
          placeholder="选择工厂名称"
          @change="queryFactoryQuotationListHandle"
      >
        <el-option
            v-for="(item, index) in data.factoryList"
            :label="item.factoryName"
            :value="item.id"
        />
      </el-select>

      <!-- 款式编号筛选条件 -->
      <el-input
          clearable
          v-model="data.styleNumber"
          placeholder="款式编号"
          @input="queryFactoryQuotationListHandle"
      >
      </el-input>

      <!-- 工作类型筛选条件 -->
      <el-select
          v-model="data.categoryId"
          clearable
          placeholder="选择工作类型"
          @change="queryFactoryQuotationListHandle"
      >
        <el-option
            v-for="item in data.categoryList"
            :label="item.category"
            :value="item.id"
        />
      </el-select>

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
      <el-table-column label="操作" align="center" width="160px">
        <template #default="scope">
          <el-button type="primary" text @click="showUpdateDialog(scope.row)">编辑</el-button>
          <el-popconfirm title="确认删除?" @confirm="deleteQuotationInfoHandle(scope.row.id)">
            <template #reference>
              <el-button type="danger" text>删除</el-button>
            </template>
          </el-popconfirm>
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
          @current-change="queryFactoryQuotationListHandle"
          @size-change="queryFactoryQuotationListHandle"
      />
    </div>

    <!--  添加或编辑成衣厂账单信息弹窗  -->
    <el-dialog
        v-model="data.addQuotationDialogVisible"
        :title="
        data.addQuotationDialogMode === 0 ? '添加成衣厂报价单' : data.addQuotationDialogMode === 1 ? '修改成衣厂报价单': ''"
        width="50%"
        center
        @closed="resetForm(quotationInfoFormRef)"
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
          <el-button @click="restQuotationInfoClickEvent(quotationInfoFormRef)">取消</el-button>
          <el-button type="primary" v-if="data.addQuotationDialogMode === 0"
                     @click="quotationValidation(quotationInfoFormRef)">
            新增
          </el-button>
          <el-button type="primary"
                     v-if="data.addQuotationDialogMode === 1" @click="quotationValidation(quotationInfoFormRef)">
            修改
          </el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>


<script setup>

import {onMounted, reactive, ref} from "vue";
import {queryCategoryList, queryFactoryList} from "../../../nwtwork/index.js";
import {
  deleteFactoryQuotationInfo,
  queryFactoryQuotationList,
  saveFactoryQuotationInfo,
  updateFactoryQuotationInfo
} from "../../../nwtwork/admin.js";
import {ElMessage} from "element-plus";


onMounted(() => {
  queryFactoryListHandle()
  queryCategoryListHandle()
  queryFactoryQuotationListHandle()
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

  addQuotationDialogVisible: false,
  addQuotationDialogMode: 0,
})

const quotationInfoFormRef = ref()

// TODO 查询成衣厂列表
const queryFactoryListHandle = async () => {
  const {data: res} = await queryFactoryList()
  data.factoryList = res.data
}

// TODO 查询工作类型列表
const queryCategoryListHandle = async () => {
  const {data: res} = await queryCategoryList()
  data.categoryList = res.data
}


// TODO 查询成衣厂报价单列表
const queryFactoryQuotationListHandle = async () => {

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
    console.log(res.data.records)
    data.tableData = res.data.records
    data.total = res.data.total
  } else {
    data.tableData = []
  }

}

// TODO 添加成衣厂报价单信息
const addFactoryQuotationInfoHandle = async () => {
  const {data: res} = await saveFactoryQuotationInfo(factoryQuotationInfoRef)
  if (res.status === 200) {
    ElMessage.success(res.message)
    await queryFactoryQuotationListHandle()
  } else {
    ElMessage.success(res.message)
  }

}

// TODO 修改成衣厂报价单信息
const updateQuotationInfoHandle = async () => {
  const {data: res} = await updateFactoryQuotationInfo(factoryQuotationInfoRef)
  if (res.status === 200) {
    ElMessage.success(res.message)
    await queryFactoryQuotationListHandle()

  } else {
    ElMessage.success(res.message)
  }
  data.addQuotationDialogVisible = false
}

// TODO 删除成衣厂报价单信息
const deleteQuotationInfoHandle = async (id) => {

  const {data: res} = await deleteFactoryQuotationInfo(id)
  if (res.status === 200) {
    ElMessage.success(res.message)
    await queryFactoryQuotationListHandle()
  } else {
    ElMessage.success(res.message)
  }

}

// 判断账单表单处于新增或是编辑状态
const quotationValidation = async formEl => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      // 判断是新增还是修改  0 新增 1 编辑
      if (data.addQuotationDialogMode === 0) {
        // 添加
        addFactoryQuotationInfoHandle()
      } else if (data.addQuotationDialogMode === 1) {
        // 编辑
        updateQuotationInfoHandle()
      }
    } else {
      ElMessage.error('请检查是否填写正确')
    }
  })
}

// 构建报价单实体
const factoryQuotationInfoRef = reactive({
  id: '',
  factoryId: '',
  styleNumber: '',
  categoryId: '',
  quotation: ''
})

// 显示修改弹窗
const showUpdateDialog = (quotationInfo) => {
  data.addQuotationDialogVisible = true
  data.addQuotationDialogMode = 1
  // 自动填写原信息
  factoryQuotationInfoRef.id = quotationInfo.id
  factoryQuotationInfoRef.factoryId = quotationInfo.factoryId
  factoryQuotationInfoRef.styleNumber = quotationInfo.styleNumber
  factoryQuotationInfoRef.categoryId = quotationInfo.categoryId
  factoryQuotationInfoRef.quotation = quotationInfo.quotation
}

// 新增按钮账单点击事件
const clickAddButton = () => {
  data.addQuotationDialogVisible = true
  data.addQuotationDialogMode = 0
}

// 编辑账单弹窗取消按钮点击事件
const restQuotationInfoClickEvent = () => {
  data.addQuotationDialogVisible = false // 关闭窗口
}

// 清空表单
const resetForm = formEl => {
  if (!formEl) return
  formEl.resetFields()
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
  flex-wrap: wrap;
  padding: 10px;
  gap: 10px;
  border-bottom: 1px solid var(--el-border-color);
}

.factoryQuotation-button > *:nth-child(1) {
  width: 70px;
}

.factoryQuotation-button > *:nth-child(2) {
  width: 150px;
}

.factoryQuotation-button > *:nth-child(3) {
  width: 150px;
}

.factoryQuotation-button > *:nth-child(4) {
  width: 150px;
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

</style>