<template>
  <div class="home-container">
    <div class="nav-container">
      <van-button type="primary" @click="clickAddEmployee">注册</van-button>
      <van-button type="primary" @click="clickAddWork">添加工作记录</van-button>
      <van-button type="primary" @click="clickSalary">计算工资</van-button>
      <!-- 日期选择组件 -->
      <van-cell
          title="选择日期"
          :value="data.date"
          @click="data.isShowDate = true"
          class="databutton"
      />
      <van-calendar
          v-model:show="data.isShowDate"
          allow-same-day
          :min-date="new Date(2020, 0, 1)"
          type="range"
          @confirm="onConfirm"
      />

      <!-- 员工选择组件 -->
      <van-dropdown-menu>
        <van-dropdown-item
            v-model="data.id"
            @change="queryJobListByEmployeeIdAndDateHandle"
            :options="data.selectEmployeeList"/>
      </van-dropdown-menu>

      <!-- 工作类型选择组件 -->
      <van-dropdown-menu>
        <van-dropdown-item
            v-model="data.categoryId"
            @change="queryJobListByEmployeeIdAndDateHandle"
            :options="data.selectCategoryList"/>
      </van-dropdown-menu>
      <!-- 成衣厂搜索组件 -->
      <el-select size="large" style="width: 10%" filterable
                 v-model="data.factoryId" placeholder="选择成衣厂"
                 @change="queryJobListByEmployeeIdAndDateHandle">
        <el-option
            v-for="(item, index) in data.factoryList"
            :key="item.id"
            :label="item.factoryName"
            :value="item.id"/>
      </el-select>
      <!-- 床号搜索组件 -->
      <el-input
          v-model="data.number"
          style="width: 10%"
          size="large"
          placeholder="要查询的床号"
          @input="queryJobListByEmployeeIdAndDateHandle"/>

      <van-button type="primary" @click="resetQueryConditionForm">重置</van-button>
    </div>

    <el-card>
      <!--  工作信息表格  -->
      <el-row>
        <el-table
            :data="data.tableData" height="60"
            stripe fit highlight-current-row show-summary
            :default-sort="{prop: 'number', order: 'descending'}"
            :summary-method="summaryQuantityAndSalary" style="height: 100%"
        >
          <el-table-column sortable prop="name" label="员工名称" align="center"/>
          <el-table-column sortable prop="factoryName" label="厂名" align="center"/>
          <el-table-column sortable prop="number" label="床号" align="center"/>
          <el-table-column prop="styleNumber" label="款式编号" align="center"/>
          <el-table-column prop="category" label="类型" align="center"/>
          <el-table-column prop="quantity" label="数量" align="center"/>
          <el-table-column prop="salary" width="95" label="本床工资(单位: 元)" align="center"/>
          <el-table-column prop="createdTime" label="日期" align="center"/>
          <el-table-column label="管理" align="center">
            <template #default="scope">
              <el-button type="danger" text @click="deleteJobInfoByIdHandler(scope.row.id)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-row>
      <!-- 分页控制 -->
      <el-row class="page-help">
        <el-pagination
            background
            :total="data.total"
            v-model:current-page="data.currentPage"
            v-model:page-size="data.pageSize"
            :page-sizes="[5, 10, 20, 50, 100]"
            layout="sizes, prev, pager, next, jumper, ->"
            @current-change="queryJobListByEmployeeIdAndDateHandle()"
            @size-change="queryJobListByEmployeeIdAndDateHandle()"
        />
      </el-row>
    </el-card>

    <!--  员工注册表单  -->
    <el-dialog v-model="data.dialogVisible" title="员工注册" width="90%" center>
      <el-form
          ref="ruleFormRef"
          :model="addEmployeeInfoRef"
          :rules="rules"
          label-position="top"
      >
        <el-form-item label="员工姓名:" prop="name">
          <el-input
              v-model="addEmployeeInfoRef.name"
              autocomplete="off"
              size="large"
          />
        </el-form-item>
        <el-form-item label="选择工作方式:" size="large" prop="modeId">
          <el-select v-model="addEmployeeInfoRef.modeId" placeholder="请选择工作类型">
            <el-option
                v-for="(item, index) in data.modeList"
                :label="item.mode"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.dialogVisible = false" size="large">取消</el-button>
          <el-button
              type="primary" :loading="data.isLoading"
              @click="submitForm(ruleFormRef)"
              size="large">确认
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!--  工资计算表单  -->
    <el-dialog v-model="data.SalaryVisible" title="工资计算" width="90%" center>
      <el-form
          ref="querySalaryFormRef"
          label-position="top"
          :model="SalaryRef"
          :rules="querySalaryRule"
      >
        <el-form-item label="员工姓名:" size="large" prop="employeeId">
          <el-select v-model="SalaryRef.employeeId" placeholder="请选择员工">
            <el-option
                v-for="(item, index) in data.employeeList"
                :label="item.text"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="计算日期:" size="large">
          <el-date-picker
              v-model="SalaryRef.startDate"
              type="date"
              format="YYYY/MM/DD"
              value-format="YYYY-MM-DD"
              placeholder="开始日期"
          />
          <el-date-picker
              v-model="SalaryRef.endDate"
              type="date"
              format="YYYY/MM/DD"
              value-format="YYYY-MM-DD"
              placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item size="large">
          <el-button
              type="primary"
              style="width: 100%"
              size="large"
              @click="querySalarySubmitForm(querySalaryFormRef)"
          >
            计算
          </el-button>
        </el-form-item>
        <el-form-item size="large">
          <el-statistic
              title="工资总和"
              Transactions
              :value="outputValue"
              style="text-align: center; margin: 0 auto; display: block"
          />
        </el-form-item>
      </el-form>
    </el-dialog>

    <!--   添加工作信息表单   -->
    <el-dialog v-model="data.addWorkVisible" title="添加工作记录" width="90%" center>
      <el-form
          ref="addJobInfoFormRef"
          :model="addJobInfoRef"
          :rules="addWorkRules"
          label-position="top"
      >
        <el-form-item label="员工姓名:" size="large" prop="employeeId">
          <el-select
              filterable
              v-model="addJobInfoRef.employeeId"
              placeholder="请选择员工姓名"
              @change="handleEmployeeChange"
          >
            <el-option
                v-for="item in data.employeeList"
                :key="item.modeId"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择厂名:" size="large" prop="factoryId">
          <el-select filterable v-model="addJobInfoRef.factoryId" placeholder="请选择厂名">
            <el-option
                v-for="(item, index) in data.factoryList"
                :key="item.id"
                :label="item.factoryName"
                :value="item.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="床号：" prop="number">
          <el-input
              v-model="addJobInfoRef.number"
              autocomplete="off"
              size="large"
              placeholder="请输入床号"
              type="text"
          />
        </el-form-item>
        <el-form-item label="选择工作类型:" size="large" prop="categoryId">
          <el-select
              v-model="addJobInfoRef.categoryId"
              placeholder="请选择工作类型"
          >
            <el-option
                v-for="(item, index) in data.categoryList"
                :label="item.category"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="款式编号：" prop="styleNumber">
          <el-input
              v-model="addJobInfoRef.styleNumber"
              autocomplete="off"
              size="large"
              placeholder="请输入款式编号"
              type="text"
          />
        </el-form-item>
        <el-form-item label="数量：" prop="quantity">
          <el-input
              v-model="addJobInfoRef.quantity"
              autocomplete="off"
              size="large"
              placeholder="请输入数量"
              type="text"
          />
        </el-form-item>
        <el-form-item label="工作日期:" size="large" prop="createdTime">
          <el-date-picker
              :editable="false"
              v-model="addJobInfoRef.createdTime"
              type="date"
              format="YYYY/MM/DD"
              value-format="YYYY-MM-DD"
              placeholder="工作记录日期"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="resetSaveJobInfoForm" size="large">取消</el-button>
          <el-button type="primary" :loading="data.isLoading" @click="addWorkSubmitForm(addJobInfoFormRef)"
                     size="large">
            确认
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {reactive, onMounted, ref} from 'vue'
import {
  queryEmployees,
  saveEmployee,
  queryFactoryList,
  saveJobInfo,
  queryCategoryList,
  queryModeList,
  queryJobListByEmployeeIdAndDate,
  querySalaryByCondition,
  deleteJobInfoById
} from '../nwtwork/index.js'
import {ElMessage} from 'element-plus'
import {useTransition} from '@vueuse/core'

// 进入页面时执行
onMounted(() => {
  queryEmployeeListHandle()
  queryFactoryListHandle()
  queryCategoryListHandle()
  queryJobListByEmployeeIdAndDateHandle()
})

const data = reactive({
  dialogVisible: false,
  addWorkVisible: false,
  SalaryVisible: false,
  isShowDate: false,
  id: Number(),
  startDate: '',  // 起止日期
  endDate: '',    // 起止日期
  factoryId: '',   // 成衣厂ID
  number: '',     // 床号
  categoryId: Number(),  // 工作类型ID
  date: '',
  employeeList: [],
  tableData: [],
  isLoading: false,
  factoryList: [],
  categoryList: [],

  selectCategoryList: [],
  selectEmployeeList: [],

  modeList: [],
  total: 0,
  currentPage: 1,
  pageSize: 10
})

const salaryTotal = ref(0)

// 动画过渡
const outputValue = useTransition(salaryTotal, {
  duration: 1000
})

// 查询员工列表
const queryEmployeeListHandle = async () => {
  const {data: res} = await queryEmployees()
  data.employeeList = res.data
  await selectEmployeeListHandle()
}

// 查询成衣厂列表
const queryFactoryListHandle = async () => {
  const {data: res} = await queryFactoryList()
  data.factoryList = res.data
}

// 查询工作类型列表
const queryCategoryListHandle = async () => {
  const {data: res} = await queryCategoryList()
  data.categoryList = res.data
  await selectCategoryListHandle()
}

// 查询工作方式列表
const queryModeListHandle = async () => {
  const {data: res} = await queryModeList()
  data.modeList = res.data
}

// 查询工作信息列表
const queryJobListByEmployeeIdAndDateHandle = async () => {
  const {data: res} = await queryJobListByEmployeeIdAndDate(
      data.id,
      data.startDate,
      data.endDate,
      data.factoryId,
      data.number,
      data.categoryId,
      data.currentPage,
      data.pageSize
  )
  if (res.status === 200) {
    data.tableData = res.data.records
    data.total = res.data.total
  }
  else {
      data.tableData = []
      ElMessage.error(res.message)

  }

}

// 计算工资请求函数
const salaryInquiryHandle = async () => {
  const {data: res} = await querySalaryByCondition(
      SalaryRef.employeeId,
      SalaryRef.startDate,
      SalaryRef.endDate
  )
  if (res.status === 200) {
    salaryTotal.value = res.data.salary
  } else {
    salaryTotal.value = 0
    ElMessage.error(res.message)
  }
}

// 员工注册请求函数
const employeeRegisterHandle = async () => {
  data.isLoading = true
  const {data: res} = await saveEmployee(addEmployeeInfoRef)
  if (res.status === 200) {
    ElMessage.success(res.message)
  } else {
    ElMessage.error(res.message)
  }
  data.isLoading = false
}

// 保存工作信息请求函数
const saveJobInfoHandle = async () => {
  const {data: res} = await saveJobInfo(addJobInfoRef)
  if (res.status === 200) {
    ElMessage.success(res.message)
    // 保存成功后 关闭弹窗
    //data.addWorkVisible = false
    // 保存成功后清除填写的工作信息
    //resetSaveJobInfoForm()
    await queryJobListByEmployeeIdAndDateHandle()
  } else {
    ElMessage.error(res.message)
  }
}

// 删除工作信息请求函数
const deleteJobInfoByIdHandler = async id => {
  const {data: res} = await deleteJobInfoById(id)
  if (res.status === 200) {
    ElMessage.success(res.message)
  } else {
    ElMessage.error(res.message)
  }
  await queryJobListByEmployeeIdAndDateHandle()
}

// 获取当前选中员工的modeId
const handleEmployeeChange = employeeId => {
  const selectedEmployee = data.employeeList.find(
      item => item.id === employeeId
  )
  addJobInfoRef.modeId = selectedEmployee ? selectedEmployee.modeId : ''
}

// 注册员工按钮点击事件
const clickAddEmployee = () => {
  data.dialogVisible = true // 打开弹窗
  queryModeListHandle()     // 查询工作方式列表
}

// 添加工作信息按钮点击事件
const clickAddWork = () => {
  data.addWorkVisible = true // 打开弹窗
  queryEmployeeListHandle()  // 查询员工列表
  queryFactoryListHandle()   // 查询成衣厂列表
  queryCategoryListHandle()  // 查询工作类型列表
}

// 计算工资按钮点击事件
const clickSalary = () => {
  data.SalaryVisible = true // 打开弹窗
}

// 绑定员工列表到Vant选项组件中
const selectEmployeeListHandle = async () => {
  data.selectEmployeeList = data.employeeList.map(item => ({
    text: item.name, // Vant 组件显示的文字
    value: item.id, // 绑定到 v-model 的值
    modeId: item.modeId
  }))
  data.selectEmployeeList.unshift({text: '选择员工', value: 0})
}

// 绑定分类列表到Vant选项组件中
const selectCategoryListHandle = async () => {
  data.selectCategoryList = data.categoryList.map(item => ({
    text: item.category, // Vant 组件显示的文字
    value: item.id, // 绑定到 v-model 的值
  }))
  data.selectCategoryList.unshift({text: '选择工作类型', value: 0})
}

// 表格自定义合计
const summaryQuantityAndSalary = ({columns, data}) => {
  const sums = []
  columns.forEach((column, index) => {
    // 第一列显示“合计”
    if (index === 0) {
      sums[index] = '合计'
      return
    }

    // 只对 quantity 和 salary 两列求和
    if (['quantity', 'salary'].includes(column.property)) {
      const total = data.reduce((sum, row) => {
        const value = Number(row[column.property])
        return !isNaN(value) ? sum + value : sum
      }, 0)
      // 工资列保留两位小数
      sums[index] =
          column.property === 'salary' ? total.toFixed(2) : total
    } else {
      sums[index] = ''
    }
  })
  return sums
}

const ruleFormRef = ref()
const addJobInfoFormRef = ref()
const querySalaryFormRef = ref()

// 添加员工信息表单参数
const addEmployeeInfoRef = reactive({
  name: '',
  modeId: Number()
})

// 添加工作信息表单参数
const addJobInfoRef = reactive({
  employeeId: '',
  factoryId: '',
  categoryId: '',
  modeId: '',
  number: '',
  styleNumber: '',
  quantity: '',
  createdTime: ''
})

// 重置按钮函数
const resetQueryConditionForm = () => {
  data.startDate = ''
  data.endDate = ''
  data.id = 0
  data.factoryId = ''
  data.number = ''
  data.categoryId = 0
  queryJobListByEmployeeIdAndDateHandle()
}

// 清除添加工作信息表单填写的参数
const resetSaveJobInfoForm = () => {
  addJobInfoFormRef.value.resetFields()
  data.addWorkVisible = false
}

const SalaryRef = reactive({
  employeeId: '',
  startDate: '',
  endDate: ''
})

// 员工注册表单校验
const submitForm = async formEl => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      // 发起员工注册请求
      employeeRegisterHandle()
    } else {
      ElMessage.error("请检查是否填写正确", fields)
    }
  })
}

// 添加工作记录表单校验
const addWorkSubmitForm = async formEl => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      saveJobInfoHandle()
    } else {
      ElMessage.error("请检查是否填写正确", fields)
    }
  })
}

// 查询计算工资表单校验
const querySalarySubmitForm = async formEl => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      salaryInquiryHandle()
    } else {
      console.log('error query!', fields)
    }
  })
}

// 日期格式化函数
const formatDate = date => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 按区间/id查询方法
const onConfirm = values => {
  const [start, end] = values
  data.isShowDate = false
  data.startDate = formatDate(start)
  data.endDate = formatDate(end)
  queryJobListByEmployeeIdAndDateHandle()
}

const onlyNumberRule = {
  pattern: /^[0-9]+$/,
  message: '只能输入数字',
  trigger: 'blur'
}

// 注册表单校验规则
const rules = reactive({
  name: [
    {required: true, message: '请输入员工姓名', trigger: 'blur'},
    {min: 2, max: 30, message: '长度在 2 到 6 个字符', trigger: 'blur'}
  ],
  modeId: [{required: true, message: '请选择工作方式', trigger: 'blur'}]
})

// 添加工作记录表单校验规则
const addWorkRules = reactive({
  employeeId: [{required: true, message: '请选择员工姓名', trigger: 'blur'}],
  factoryId: [{required: true, message: '请选择厂名', trigger: 'blur'}],
  categoryId: [{required: true, message: '请选择工作类型', trigger: 'blur'}],
  number: [{required: true, message: '请输入床号', trigger: 'blur'}],
  styleNumber: [
    {required: true, message: '请输入款式编号', trigger: 'blur'},
    onlyNumberRule
  ],
  quantity: [
    {required: true, message: '请输入数量', trigger: 'blur'},
    onlyNumberRule
  ],
  createdTime: [{required: true, message: '请选择工作日期', trigger: 'blur'}]
})

// 计算工资表单校验规则
const querySalaryRule = reactive({
  employeeId: [
    {required: true, message: '请选择员工姓名', trigger: 'blur'},
    {
      validator: (rule, value, callback) => {
        if (value === 0) {
          callback(new Error('请选择有效的员工姓名'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})
</script>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  height: 100vh; /* 占满视口高度 */
}

.nav-container {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  padding: 20px 20px 0 20px;
}

:deep(.el-card) {
  margin: 20px;
  height: calc(100% - 85px);
}

:deep(.van-dropdown-menu__title::after) {
  display: none;
}

:deep(.el-card__body) {
  height: calc(100% - 40px);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

:deep(.el-row) {
  overflow: auto;
}

.page-help {
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  height: 50px !important;
}

:deep(.van-cell) {
  width: auto;
}

:deep(.van-cell__value) {
  flex: 0;
}

:deep(.van-dropdown-menu__bar) {
  background-color: #1989fa;
  border-radius: 4px;
  height: 45px;
  color: white;
}

:deep(.van-dropdown-menu__title) {
  color: white;
}

:deep(.el-table__inner-wrapper) {
  font-size: 16px;
}

:deep(.el-form-item__label) {
  font-size: 16px;
}

.databutton {
  background-color: #1989fa;
  border-radius: 4px;
  height: 45px;
  color: white;
}

.drlist-item {
  padding: 10px 15px;
}

:deep(.el-form-item__content) {
  flex-wrap: nowrap;
  gap: 10px;
}

:deep(.el-statistic__number::after) {
  content: '元';
}
</style>
