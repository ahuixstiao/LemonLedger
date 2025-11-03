<template>
  <div class="factoryBill-container">

    <el-card>
      <!-- 筛选条件 -->
      <el-row class="factoryBill-button">
        <el-button type="primary" @click="clickAddBill">添加</el-button>
        <!-- 成衣厂筛选条件 -->
        <el-select
            v-model="data.factoryId"
            filterable
            clearable
            placeholder="选择工厂名称"
            @change="queryFactoryBillListHandle"
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
            @input="queryFactoryBillListHandle"
        >
        </el-input>

        <!-- 工作类型筛选条件 -->
        <el-select
            v-model="data.categoryId"
            clearable
            placeholder="选择工作类型"
            @change="queryFactoryBillListHandle"
        >
          <el-option
              v-for="(item, index) in data.categoryList"
              :label="item.category"
              :value="item.id"/>
        </el-select>

        <!-- 数据库记录状态筛选条件 -->
        <el-select v-model="data.flag" placeholder="状态" @change="queryFactoryBillListHandle">
          <el-option label="正常" :value="0"/>
          <el-option label="删除" :value="1"/>
        </el-select>

        <!-- 日期筛选条件 -->
        <el-date-picker
            v-model="data.startDate"
            type="date"
            format="YYYY/MM/DD"
            value-format="YYYY-MM-DD"
            placeholder="开始日期"
            @change="queryFactoryBillListHandle"
        />
        <el-date-picker
            v-model="data.endDate"
            type="date"
            format="YYYY/MM/DD"
            value-format="YYYY-MM-DD"
            placeholder="结束日期"
            @change="queryFactoryBillListHandle"
        />
      </el-row>
      <!-- 成衣厂账单表格 -->
      <el-row style="display: flex; flex-direction: column; height: 73vh;">
        <el-table :data="data.tableData" style="width: 100%" stripe fit highlight-current-row empty-text="暂无数据">
          <!--<el-table-column prop="id" label="ID" align="center"/>-->
          <el-table-column prop="factoryName" sortable label="厂名" align="center"/>
          <el-table-column prop="number" sortable label="床号" align="center"/>
          <el-table-column prop="styleNumber" label="款式编号" align="center"/>
          <el-table-column prop="category" label="类型" align="center"/>
          <el-table-column prop="quantity" label="数量" align="center"/>
          <el-table-column prop="createdTime" label="日期" align="center"/>
          <el-table-column prop="flag" label="状态" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.flag === 0" type="success">正常</el-tag>
              <el-tag v-if="scope.row.flag === 1" type="danger">删除</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center">
            <template #default="scope">
              <el-button type="primary" text @click="billEditHandle(scope.row)"> 编辑</el-button>
              <el-popconfirm title="确认删除?" @confirm="billDeleteHandle(scope.row.id)">
                <template #reference>
                  <el-button type="danger" text>删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </el-row>
      <!-- 分页组件 -->
      <el-row class="factoryBill-page">
        <el-pagination
            background
            :total="data.total"
            v-model:current-page="data.currentPage"
            v-model:page-size="data.pageSize"
            :page-sizes="[5, 10, 20, 50, 100]"
            layout="sizes, prev, pager, next, jumper, ->"
            @current-change="queryFactoryBillListHandle()"
            @size-change="queryFactoryBillListHandle()"/>
      </el-row>
    </el-card>

    <!--  添加成衣厂账单弹窗  -->
    <el-dialog
        v-model="data.addBillDialogVisible"
        :title="data.addBillDialogMode === 0? '添加成衣厂账单': data.addBillDialogMode === 1? '修改成衣厂账单': ''"
        width="50%"
        center
    >
      <el-form ref="billFormRef" :model="addFactoryBillInfoRef" :rules="addBillInfoRules"
               label-width="auto" label-position="left"
      >
        <el-form-item size="large" label="工厂名称:" prop="factoryId">
          <el-select v-model="addFactoryBillInfoRef.factoryId" placeholder="选择工厂名称">
            <el-option v-for="item in data.factoryList" :label="item.factoryName" :value="item.id"/>
          </el-select>
        </el-form-item>

        <el-form-item size="large" label="床号" prop="number">
          <el-input v-model="addFactoryBillInfoRef.number" placeholder="输入床号"/>
        </el-form-item>

        <el-form-item size="large" label="款式编号:" prop="styleNumber">
          <el-input v-model="addFactoryBillInfoRef.styleNumber" placeholder="输入款式编号"/>
        </el-form-item>

        <el-form-item size="large" label="工作类型:" prop="categoryId">
          <el-select v-model="addFactoryBillInfoRef.categoryId" placeholder="选择工作类型">
            <el-option v-for="item in data.categoryList" :label="item.category" :value="item.id"/>
          </el-select>
        </el-form-item>

        <el-form-item size="large" label="数量:" prop="quantity">
          <el-input v-model="addFactoryBillInfoRef.quantity" placeholder="输入数量"/>
        </el-form-item>

        <el-form-item size="large" label="账单日期:" prop="createdTime">
          <el-date-picker
              :editable="false"
              v-model="addFactoryBillInfoRef.createdTime"
              type="date"
              format="YYYY/MM/DD"
              value-format="YYYY-MM-DD"
              placeholder="账单日期"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.addBillDialogVisible = false">取消</el-button>
          <el-button
              v-if="data.addBillDialogMode === 0"
              type="primary"
              @click="billValidation(billFormRef)"
          >
            确认
          </el-button>
          <el-button
              v-if="data.addBillDialogMode === 1"
              type="primary"
              @click="billValidation(billFormRef)"
          >
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {reactive, onMounted, ref} from 'vue'
import {queryCategoryList, queryFactoryList} from '../../../nwtwork/index.js';
import {queryFactoryBillList, saveFactoryBillInfo} from "../../../nwtwork/admin.js";
import {ElMessage} from "element-plus";


onMounted(() => {
  queryFactoryListHandle()
  queryFactoryBillListHandle()
  queryCategoryListHandle()
})

const data = reactive({
  tableData: [],
  factoryList: [],
  styleNumberList: [],
  categoryList: [],
  factoryId: '',
  number: '',
  startDate: '',
  endDate: '',
  styleNumber: '',
  categoryId: '',
  flag: 0,
  currentPage: 1,
  pageSize: 10,
  total: 0,
  addBillDialogVisible: false,
  addBillDialogMode: 0 // 0为新增、1为编辑
})

// 查询厂名列表
const queryFactoryListHandle = async () => {
  const {data: res} = await queryFactoryList()
  data.factoryList = res.data
}

// 查询账单信息
const queryFactoryBillListHandle = async () => {
  const {data: res} = await queryFactoryBillList(
      data.factoryId, data.number, data.styleNumber, data.categoryId, data.flag,
      data.startDate, data.endDate,
      data.currentPage, data.pageSize
  )
  // 判断请求是否成功
  if (res.status === 200) {
    data.tableData = res.data.records
    data.total = res.data.total
  } else {
    data.tableData = []
  }
}

// 查询工作类型
const queryCategoryListHandle = async () => {
  const {data: res} = await queryCategoryList()
  data.categoryList = res.data
}


// 编辑账单数据请求
const billEditHandle = item => {
  data.addBillDialogVisible = true
  data.addBillDialogMode = 1
  addFactoryBillInfoRef.id = item.id
  addFactoryBillInfoRef.factoryId = item.factoryId
  addFactoryBillInfoRef.categoryId = item.categoryId
  addFactoryBillInfoRef.number = item.number
  addFactoryBillInfoRef.styleNumber = item.styleNumber
}

// 新增账单请求
const addFactoryBillInfoHandle = async () => {
  //resetForm(billFormRef)
  const {data: res} = await saveFactoryBillInfo(addFactoryBillInfoRef)
  if (res.status === 200) {
    ElMessage.success(res.message)
    await queryFactoryBillListHandle()
  }else {
    ElMessage.error(res.message)
  }

}

// 修改账单请求
const editBillHandle = () => {
  resetForm(billFormRef)
}

// 删除账单数据请求
const billDeleteHandle = id => {

}

const billFormRef = ref()
const addFactoryBillInfoRef = reactive({
  id: '',
  factoryId: '',
  number: '',
  styleNumber: '',
  categoryId: '',
  quantity: '',
  createdTime: ''
})

const clickAddBill = () => {
  data.addBillDialogVisible = true
  data.addBillDialogMode = 0
}

const onlyNumberRule = {
  pattern: /^[0-9]+$/,
  message: '只能输入数字',
  trigger: 'blur'
}

// 添加账单记录校验规则
const addBillInfoRules = reactive({
  factoryId: [{required: true, message: '请选择厂名', trigger: 'blur'}],
  categoryId: [{required: true, message: '请选择工作类型', trigger: 'blur'}],
  number: [{required: true, message: '请输入床号', trigger: 'blur'}],
  styleNumber: [{required: true, message: '请输入款式编号', trigger: 'blur'}],
  quantity: [{required: true, message: '请输入数量', trigger: 'blur'}, onlyNumberRule],
  createdTime: [{required: true, message: '请选择账单日期', trigger: 'blur'}]
})

// 添加账单记录表单校验
const billValidation = async formEl => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      // 判断是新增还是修改
      if (data.addBillDialogMode === 0) {
        addFactoryBillInfoHandle()
      } else if (data.addBillDialogMode === 1) {
        editBillHandle()
      }
    } else {
      ElMessage.error("请检查是否填写正确")
    }
  })
}

const resetForm = formEl => {
  if (!formEl) return
  formEl.resetFields()
}

</script>

<style scoped>
.factoryBill-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.factoryBill-button {
  display: flex;
  flex-wrap: wrap;
  padding: 10px;
  margin-top: -20px;
  gap: 10px;
  border-bottom: 1px solid var(--el-border-color);
}

.factoryBill-button > *:nth-child(2) {
  width: 150px;
}

.factoryBill-button > *:nth-child(3) {
  width: 150px;
}

.factoryBill-button > *:nth-child(4) {
  width: 150px;
}

.factoryBill-button > *:nth-child(5) {
  width: 80px;
}

.factoryBill-button > *:nth-child(6) {
  width: 140px;
}

.factoryBill-button > *:nth-child(7) {
  width: 140px;
}

.factoryBill-page {
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  height: 50px !important;
}
</style>
