<template>
  <div class="factorybill-container">
    <div class="factorybill-button">
      <el-button type="primary" @click="clickAddBill">添加</el-button>
      <el-select
        v-model="data.factoryId"
        filterable
        clearable
        placeholder="请选择工厂名称"
      >
        <el-option
          v-for="(item, index) in data.factoryList"
          :label="item.factoryName"
          :value="item.id"
        />
      </el-select>
      <el-select
        v-model="data.styleNumber"
        filterable
        clearable
        placeholder="请选择款式编号"
      >
        <el-option
          v-for="(item, index) in data.styleNumberList"
          :label="item.styleNumber"
          :value="item.styleNumber"
        />
      </el-select>
      <el-select
        v-model="data.categoryId"
        filterable
        clearable
        placeholder="请选择工作类型"
      >
        <el-option
          v-for="(item, index) in data.categoryList"
          :label="item.category"
          :value="item.categoryId"
        />
      </el-select>
      <el-select v-model="data.falg" filterable placeholder="是否删除">
        <el-option label="正常" :value="0" />
        <el-option label="删除" :value="1" />
      </el-select>
      <el-date-picker
        v-model="data.startDate"
        type="date"
        format="YYYY/MM/DD"
        value-format="YYYY-MM-DD"
        placeholder="开始日期"
      />
      <el-date-picker
        v-model="data.endDate"
        type="date"
        format="YYYY/MM/DD"
        value-format="YYYY-MM-DD"
        placeholder="结束日期"
      />
      <el-button icon="Search" @click="queryFactoryBillListHandle" />
    </div>
    <div class="factorybill-data">
      <el-table :data="data.tableData" style="width: 100%">
        <el-table-column prop="id" label="id" />
        <el-table-column prop="factoryName" label="工厂名称" />
        <el-table-column prop="styleNumber" label="样式款号" />
        <el-table-column prop="number" label="床号 " />
        <el-table-column prop="quantity" label="数量" />
        <el-table-column prop="categoryId" label="工作类型" />
        <el-table-column label="操作" width="160">
          <template #default="scope">
            <el-button @click="billEditHandle(scope.row)"> 编辑 </el-button>
            <el-popconfirm
              title="你确定要删除这条数据吗"
              @confirm="billDeltetHandle(scope.row.id)"
            >
              <template #reference>
                <el-button type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="factorybill-page">
      <el-pagination
        background
        :total="data.total"
        v-model:current-page="data.currentPage"
        v-model:page-size="data.pageSize"
        :page-sizes="[5, 10, 20, 50, 100]"
        layout="sizes, prev, pager, next, jumper, ->"
        @current-change="queryFactoryBillListHandle()"
        @size-change="queryFactoryBillListHandle()"
      />
    </div>
    <el-dialog
      v-model="data.addBillDialogVisible"
      :title="
        data.addBillDialogMode === 0
          ? '添加账单'
          : data.addBillDialogMode === 1
          ? '修改账单'
          : ''
      "
      width="50%"
      center
    >
      <el-form
        ref="BillFormRef"
        :model="addBillForm"
        :rules="rules"
        label-width="auto"
        label-position="left"
      >
        <el-form-item label="工厂名称" prop="factoryId">
          <el-select
            v-model="addBillForm.factoryId"
            placeholder="请选择工厂名称"
          >
            <el-option
              v-for="item in data.factoryList"
              :label="item.factoryName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="样式编号" prop="styleNumber">
          <el-select
            v-model="addBillForm.styleNumber"
            placeholder="请选择样式编号"
          >
            <el-option
              v-for="item in addBillForm.styleNumberList"
              :label="item.styleNumber"
              :value="item.styleNumber"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="床号" prop="number">
          <el-input v-model="addBillForm.number" placeholder="请输入床号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.addBillDialogVisible = false">取消</el-button>
          <el-button
            v-if="data.addBillDialogMode === 0"
            type="primary"
            @click="BillValidation(BillFormRef)"
          >
            确认
          </el-button>
          <el-button
            v-if="data.addBillDialogMode === 1"
            type="primary"
            @click="BillValidation(BillFormRef)"
          >
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, onMounted, ref } from 'vue'
import {
  queryFactoryBillList,
  queryFactoryList
} from '../../../nwtwork/index.js'

onMounted(() => {
  queryFactoryListHandle()
  queryFactoryBillListHandle()
})

const data = reactive({
  tableData: [],
  factoryList: [],
  styleNumberList: [],
  factoryId: '',
  number: '',
  startDate: '',
  endDate: '',
  styleNumber: '',
  categoryId: '',
  falg: 0,
  currentPage: 1,
  pageSize: 10,
  total: 0,
  addBillDialogVisible: false,
  addBillDialogMode: 0 // 0为新增、1为编辑
})

// 查询厂名列表
const queryFactoryListHandle = async () => {
  const { data: res } = await queryFactoryList()
  data.factoryList = res.data
}
// 查询账单信息
const queryFactoryBillListHandle = async () => {
  const { data: res } = await queryFactoryBillList(
    data.factoryId,
    data.number,
    data.startDate,
    data.endDate,
    data.styleNumber,
    data.categoryId,
    data.currentPage,
    data.pageSize
  )
  data.tableData = res.data
  console.log(data.tableData)
}

const BillFormRef = ref()
const addBillForm = reactive({
  id: '',
  factoryId: '',
  number: '',
  categoryId: ''
})

const clickAddBill = () => {
  data.addBillDialogVisible = true
  data.addBillDialogMode = 0
}

const addBillHandle = () => {
  resetForm(BillFormRef)
}
const editBillHandle = () => {
  resetForm(BillFormRef)
}
const rules = reactive({
  factoryId: [{ required: true, message: '请选择厂名', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择工作类型', trigger: 'blur' }],
  number: [{ required: true, message: '请输入床号', trigger: 'blur' }],
  styleNumber: [{ required: true, message: '请输入款式编号', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }]
})

// 添加账单记录表单校验
const BillValidation = async formEl => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      // 判断是新增还是修改
      if (data.addBillDialogMode === 0) {
        addBillHandle()
      } else if (data.addBillDialogMode === 1) {
        editBillHandle()
      }
      console.log('submit!')
    } else {
      console.log('error submit!', fields)
    }
  })
}

const resetForm = formEl => {
  if (!formEl) return
  formEl.resetFields()
}

// 编辑账单处理
const billEditHandle = item => {
  data.addBillDialogVisible = true
  data.addBillDialogMode = 1
  addBillForm.id = item.id
  addBillForm.factoryId = item.factoryId
  addBillForm.categoryId = item.categoryId
  addBillForm.number = item.number
}
// 删除数据处理
const billDeltetHandle = id => {
  console.log(id)
}
</script>

<style scoped>
.factorybill-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.factorybill-button {
  display: flex;
  flex-wrap: wrap;
  padding: 10px;
  gap: 10px;
  border-bottom: 1px solid var(--el-border-color);
}
.factorybill-button > *:nth-child(2) {
  width: 150px;
}
.factorybill-button > *:nth-child(3) {
  width: 150px;
}
.factorybill-button > *:nth-child(4) {
  width: 150px;
}
.factorybill-button > *:nth-child(5) {
  width: 80px;
}
.factorybill-button > *:nth-child(6) {
  width: 140px;
}
.factorybill-button > *:nth-child(7) {
  width: 140px;
}
.factorybill-data {
  flex: 1;
  overflow: auto;
}
.factorybill-page {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 15px;
}
</style>
