<template>
  <div class="factoryBill-container">
    <!-- 筛选条件 -->
    <div class="factoryBill-button">
      <el-button type="primary" @click="clickAddBill">添加</el-button>
      <el-button type="primary" @click="clickBill">账单</el-button>
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
            v-for="item in data.categoryList"
            :label="item.category"
            :value="item.id"
        />
      </el-select>

      <!-- 数据记录状态筛选条件 -->
      <el-select
          v-model="data.flag"
          placeholder="状态"
          @change="queryFactoryBillListHandle"
      >
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
      <!-- 重置按钮 -->
      <el-button type="primary" @click="resetQueryCondition">重置</el-button>
    </div>

    <!-- 成衣厂账单表格 -->
    <el-table
        :data="data.tableData"
        style="width: 100%"
        stripe
        fit
        highlight-current-row
        show-summary
        empty-text="暂无数据"
        :summary-method="summaryQuantityAndBill"
    >
      <el-table-column prop="factoryName" sortable label="厂名" align="center"/>
      <el-table-column prop="number" sortable label="床号" align="center"/>
      <el-table-column prop="styleNumber" label="款式编号" align="center">
        <template #default="scope">
          <el-select
              filterable
              v-model="scope.row.styleNumber"
              placeholder="选择款式编号"
              @click="queryFactoryStyleNumberListHandle(scope.row.factoryId)"
              @change="updateBillStyleOrCategoryHandle(scope.row.id, scope.row.factoryId, scope.row.styleNumber, scope.row.categoryId, scope.row.quantity)"
          >
            <el-option
                v-for="item in data.styleNumberList"
                :key="item.id"
                :label="item.styleNumber"
                :value="item.styleNumber"
            />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="工作类型" align="center">
        <template #default="scope">
          <el-select
              v-model="scope.row.categoryId"
              placeholder="选择工作类型"
              @change="updateBillStyleOrCategoryHandle(scope.row.id, scope.row.factoryId, scope.row.styleNumber, scope.row.categoryId, scope.row.quantity)"
          >
            <el-option
                v-for="item in data.categoryList"
                :key="item.id"
                :label="item.category"
                :value="item.id"
            />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" align="center"/>
      <el-table-column prop="bill" label="账单" align="center"/>
      <el-table-column prop="createdDate" label="日期" align="center"/>
      <el-table-column prop="flag" label="状态" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.flag === 0" type="success">正常</el-tag>
          <el-tag v-if="scope.row.flag === 1" type="danger">删除</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160px">
        <template #default="scope">
          <el-button type="primary" text @click="showUpdateBillInfoDialog(scope.row)">编辑</el-button>
          <el-popconfirm title="确认删除?" @confirm="billDeleteHandle(scope.row.id)">
            <template #reference>
              <el-button v-if="scope.row.flag === 0" type="danger" text>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <div class="factoryBill-page">
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

    <!--  添加或编辑成衣厂账单信息弹窗  -->
    <el-dialog
        v-model="data.addBillDialogVisible"
        :title="
        data.addBillDialogMode === 0 ? '添加成衣厂账单' : data.addBillDialogMode === 1 ? '修改成衣厂账单': ''"
        width="50%"
        center
        @closed="resetForm(billInfoFormRef)"
    >
      <el-form
          ref="billInfoFormRef"
          :model="factoryBillInfoRef"
          :rules="addBillInfoRules"
          label-width="auto"
          label-position="left"
      >
        <el-form-item size="large" label="工厂名称:" prop="factoryId">
          <el-select
              filterable
              v-model="factoryBillInfoRef.factoryId"
              placeholder="选择工厂名称"
          >
            <el-option
                v-for="item in data.factoryList"
                :label="item.factoryName"
                :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item size="large" label="床号" prop="number">
          <el-input
              v-model="factoryBillInfoRef.number"
              placeholder="输入床号"
          />
        </el-form-item>

        <el-form-item size="large" label="款式编号:" prop="styleNumber">
          <el-select
              filterable
              v-model="factoryBillInfoRef.styleNumber"
              placeholder="输入款式编号"
              @focus="queryFactoryStyleNumberListHandle(factoryBillInfoRef.factoryId)"
          >
            <el-option
                v-for="item in data.styleNumberList"
                :label="item.styleNumber"
                :value="item.styleNumber"
            />
          </el-select>
        </el-form-item>

        <el-form-item size="large" label="工作类型:" prop="categoryId">
          <el-select
              v-model="factoryBillInfoRef.categoryId"
              placeholder="选择工作类型"
          >
            <el-option
                v-for="item in data.categoryList"
                :label="item.category"
                :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item size="large" label="数量:" prop="quantity">
          <el-input
              v-model="factoryBillInfoRef.quantity"
              placeholder="输入数量"
          />
        </el-form-item>
        <el-form-item size="large" label="账单日期:" prop="createdDate">
          <el-date-picker
              :editable="false"
              v-model="factoryBillInfoRef.createdDate"
              type="date"
              format="YYYY/MM/DD"
              value-format="YYYY-MM-DD"
              placeholder="账单日期"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="resetBillInfoClickEvent(billInfoFormRef)">取消</el-button>
          <el-button type="primary" v-if="data.addBillDialogMode === 0"
                     @click="billValidation(billInfoFormRef)">
            添加
          </el-button>
          <el-button type="primary"
                     v-if="data.addBillDialogMode === 1" @click="billValidation(billInfoFormRef)">
            编辑
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!--  账单计算表单弹窗  -->
    <el-dialog v-model="data.billVisible" title="成衣厂账单" width="50%" center>
      <el-form
          ref="statisticalBillFormRef"
          label-position="top"
          :model="statisticalBillRef"
          :rules="statisticalBillRules"
      >
        <el-form-item label="成衣厂名称:" size="large" prop="factoryId">
          <el-select v-model="statisticalBillRef.factoryId" placeholder="请选择成衣厂">
            <el-option
                v-for="(item) in data.factoryList"
                :label="item.factoryName"
                :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="计算日期:" size="large">
          <el-date-picker
              v-model="statisticalBillRef.startDate"
              type="date"
              format="YYYY/MM/DD"
              value-format="YYYY-MM-DD"
              placeholder="开始日期"
          />
          <el-date-picker
              v-model="statisticalBillRef.endDate"
              type="date"
              format="YYYY/MM/DD"
              value-format="YYYY-MM-DD"
              placeholder="结束日期"
          />
        </el-form-item>
        <!-- 计算账单 -->
        <el-form-item size="large">
          <el-button
              type="primary"
              style="width: 100%"
              size="large"
              @click="statisticalBillForm(statisticalBillFormRef)"
          >
            计算并导出账单
          </el-button>
        </el-form-item>
        <el-form-item size="large">
          <el-statistic
              title="账单总和"
              Transactions
              :value="outputValue"
              style="text-align: center; margin: 0 auto; display: block"
          />
        </el-form-item>
      </el-form>
    </el-dialog>

  </div>
</template>

<script setup>
import {reactive, onMounted, ref} from 'vue'
import {queryCategoryList, queryFactoryList} from '../../../nwtwork/index.js'
import {
  deleteFactoryBillInfo,
  editFactoryBillInfo, exportFactoryBillExcel,
  queryFactoryQuotationStyleNumberList
} from '../../../nwtwork/admin.js'
import {
  queryFactoryBillList,
  saveFactoryBillInfo,
  statisticalFactoryBill
} from '../../../nwtwork/admin.js'
import {ElMessage} from 'element-plus'
import {useTransition} from '@vueuse/core'

onMounted(() => {
  queryFactoryListHandle()
  queryFactoryBillListHandle()
  queryCategoryListHandle()
})

const data = reactive({
  billVisible: false,
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
  addBillDialogMode: 0, // 0为新增、1为编辑
})

// 账单总数
const billTotal = ref(0)

// 动画过渡
const outputValue = useTransition(billTotal, {
  duration: 1000
})

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

// TODO 查询成衣厂报价单的款式编号列表
const queryFactoryStyleNumberListHandle = async (factoryId) => {
  const {data: res} = await queryFactoryQuotationStyleNumberList(factoryId)
  if (res.status === 200) {
    data.styleNumberList = res.data
  } else {
    ElMessage.error(res.message)
  }
}

// TODO 查询成衣厂账单列表
const queryFactoryBillListHandle = async () => {
  const {data: res} = await queryFactoryBillList(
      data.factoryId,
      data.number,
      data.styleNumber,
      data.categoryId,
      data.flag,
      data.startDate,
      data.endDate,
      data.currentPage,
      data.pageSize
  )
  // 判断请求是否成功
  if (res.status === 200) {
    data.tableData = res.data.records
    data.total = res.data.total
  } else {
    data.tableData = []
  }
}

// TODO 统计并导出账单
const statisticalAndExportFactoryBillHandle = async () => {
  const {data: res} = await statisticalFactoryBill(
      statisticalBillRef.factoryId,
      statisticalBillRef.startDate,
      statisticalBillRef.endDate
  )
  // 判断账单是否计算成功
  if (res.status === 200) {
    // 将计算结果赋值给账单总数
    billTotal.value = res.data.bill

    // 导出账单
    await exportFactoryBillExcelHandle()

  } else {
    billTotal.value = 0
    ElMessage.error(res.message)
  }
}

// TODO 导出账单Excel
const exportFactoryBillExcelHandle = async () => {
  // 计算成功后导出账单
  const res = await exportFactoryBillExcel(
      statisticalBillRef.factoryId,
      statisticalBillRef.startDate,
      statisticalBillRef.endDate
  )

  // 获取响应头中的文件名
  let fileName = '成衣厂账单.xlsx'
  const contentDisposition = res.headers['content-disposition']
  if (contentDisposition) {
    const match = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
    if (match && match[1]) {
      fileName = decodeURIComponent(match[1].replace(/['"]/g, ''))
    }
  }

  // 创建 blob 对象
  const blob = new Blob([res.data], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  })

  // 创建下载链接
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url

  // 设置下载文件名
  link.download = fileName

  // 添加到 DOM 并触发点击
  document.body.appendChild(link)
  link.click()

  // 清理
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)

  // 提示下载成功
  ElMessage.success('下载成功')

}

// TODO 新增账单请求
const addFactoryBillInfoHandle = async () => {
  //resetForm(billFormRef)
  const {data: res} = await saveFactoryBillInfo(factoryBillInfoRef)
  if (res.status === 200) {
    ElMessage.success(res.message)
    await queryFactoryBillListHandle()
  } else {
    ElMessage.error(res.message)
  }
}

// TODO 修改账单请求
const updateBillInfoHandle = async () => {
  const {data: res} = await editFactoryBillInfo(factoryBillInfoRef)
  if (res.status === 200) {
    ElMessage.success(res.message)
    await queryFactoryBillListHandle()
  } else {
    ElMessage.error(res.message)
  }
  data.addBillDialogVisible = false
}

// TODO 修改账单款式编号或工作类型请求
const updateBillStyleOrCategoryHandle = async (id, factoryId, styleNumber, categoryId, quantity) => {
  const billInfo = {id, factoryId, styleNumber, categoryId, quantity}
  const {data: res} = await editFactoryBillInfo(billInfo)
  if (res.status === 200) {
    ElMessage.success(res.message)
    await queryFactoryBillListHandle()
  } else {
    ElMessage.error(res.message)
  }
  data.addBillDialogVisible = false
}

// TODO 删除账单数据请求
const billDeleteHandle = async (id) => {
  const {data: res} = await deleteFactoryBillInfo(id)
  if (res.status === 200) {
    ElMessage.success(res.message)
    await queryFactoryBillListHandle()
  } else {
    ElMessage.error(res.message)
  }
}

// 查询指定成衣厂账单表单提交校验函数
const statisticalBillForm = async formEl => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      // 校验通过后查询并导出账单
      statisticalAndExportFactoryBillHandle()
    }
  })
}

const statisticalBillFormRef = ref()
const billInfoFormRef = ref()

// 构建统计的账单实体
const statisticalBillRef = reactive({
  factoryId: '',
  startDate: '',
  endDate: ''
})

// 构建账单实体
const factoryBillInfoRef = reactive({
  id: '',
  factoryId: '',
  number: '',
  styleNumber: '',
  categoryId: '',
  quantity: '',
  createdDate: ''
})

// 显示修改弹窗
const showUpdateBillInfoDialog = (bill) => {
  data.addBillDialogVisible = true
  data.addBillDialogMode = 1
  // 自动填写原信息
  factoryBillInfoRef.id = bill.id
  factoryBillInfoRef.factoryId = bill.factoryId
  factoryBillInfoRef.categoryId = bill.categoryId
  factoryBillInfoRef.number = bill.number
  factoryBillInfoRef.styleNumber = bill.styleNumber
  factoryBillInfoRef.createdDate = bill.createdDate
  factoryBillInfoRef.quantity = bill.quantity
}

// 判断账单表单处于新增或是编辑状态
const billValidation = async formEl => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      // 判断是新增还是修改  0 新增 1 编辑
      if (data.addBillDialogMode === 0) {
        // 添加
        addFactoryBillInfoHandle()
      } else if (data.addBillDialogMode === 1) {
        // 编辑
        updateBillInfoHandle()
      }
    } else {
      ElMessage.error('请检查是否填写正确')
    }
  })
}

// 表格自定义合计
const summaryQuantityAndBill = ({columns, data}) => {
  const sums = []
  columns.forEach((column, index) => {
    // 第一列显示“合计”
    if (index === 0) {
      sums[index] = '合计'
      return
    }

    // 只对 quantity 和 salary 两列求和
    if (['quantity', 'bill'].includes(column.property)) {
      const total = data.reduce((sum, row) => {
        const value = Number(row[column.property])
        return !isNaN(value) ? sum + value : sum
      }, 0)
      // 工资列保留两位小数
      sums[index] =
          column.property === 'bill' ? total.toFixed(2) : total
    } else {
      sums[index] = ''
    }
  })
  return sums
}

// 新增按钮账单点击事件
const clickAddBill = () => {
  data.addBillDialogVisible = true
  data.addBillDialogMode = 0
}

// 账单按钮点击事件
const clickBill = () => {
  data.billVisible = true // 打开弹窗
}

// 重置按钮点击事件
const resetQueryCondition = () => {
  data.factoryId = ''
  data.styleNumber = ''
  data.categoryId = ''
  data.flag = 0
  data.startDate = ''
  data.endDate = ''
  queryFactoryBillListHandle()
}

// 编辑账单弹窗取消按钮点击事件
const resetBillInfoClickEvent = () => {
  data.addBillDialogVisible = false // 关闭窗口
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
  quantity: [
    {required: true, message: '请输入数量', trigger: 'blur'},
    onlyNumberRule
  ],
  createdDate: [{required: true, message: '请选择账单日期', trigger: 'blur'}]
})

// 统计成衣厂账单校验规则
const statisticalBillRules = reactive({
  factoryId: [{required: true, message: '请选择成衣厂', trigger: 'blur'}]
})

// 清空表单
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
  justify-content: space-between;
}

.factoryBill-button {
  display: flex;
  flex-wrap: wrap;
  padding: 10px;
  gap: 10px;
  border-bottom: 1px solid var(--el-border-color);
}

.factoryBill-button > *:nth-child(1) {
  width: 70px;
  margin-right: -11px;
}

.factoryBill-button > *:nth-child(2) {
  width: 70px;
}

.factoryBill-button > *:nth-child(3) {
  width: 150px;
}

.factoryBill-button > *:nth-child(4) {
  width: 150px;
}

.factoryBill-button > *:nth-child(5) {
  width: 150px;
}

.factoryBill-button > *:nth-child(6) {
  width: 100px;
}

.factoryBill-button > *:nth-child(7) {
  width: 150px;
}

.factoryBill-button > *:nth-child(8) {
  width: 150px;
}

.factoryBill-page {
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

:deep(.el-statistic__number::after) {
  content: '元';
}
</style>
