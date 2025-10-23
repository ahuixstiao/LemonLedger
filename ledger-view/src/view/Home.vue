<template>
  <div class="home-container">
    <div class="nav-container">
      <van-button type="primary" @click="clickAddEmployee">注册</van-button>
      <van-button type="primary" @click="clickAddWork">添加工作记录</van-button>
      <van-button type="primary" @click="clickSalary">计算工资</van-button>
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
      <!--   员工选择组件   -->
      <van-dropdown-menu>
        <van-dropdown-item
          v-model="data.id"
          @change="queryJobListByEmployeeIdAndDateHandle"
          :options="data.employeeList"
        />
      </van-dropdown-menu>
      <van-button type="primary" @click="resetQueryConditionForm"
        >重置</van-button
      >
    </div>

    <!--  工作信息列表  -->
    <!-- <div class="list-container"> -->
    <el-card>
      <el-row>
        <el-table
          :data="data.tableData"
          height="60"
          stripe
          fit
          highlight-current-row
          show-summary
          style="height: 100%"
        >
          <el-table-column prop="name" label="员工名称" />
          <el-table-column prop="factoryName" label="厂名" />
          <el-table-column prop="number" label="床号" />
          <el-table-column prop="styleNumber" label="款式编号" />
          <el-table-column prop="category" label="类型" />
          <el-table-column prop="quantity" label="数量" />
          <el-table-column
            prop="salary"
            width="95"
            label="本床工资(单位: 元)"
          />
          <el-table-column prop="createdTime" label="日期" />
          <el-table-column label="管理">
            <template #default="scope">
              <el-button
                type="text"
                @click="deleteJobInfoByIdHandler(scope.row.id)"
                >删除</el-button
              >
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
    <!-- </div> -->

    <!--  员工注册表单  -->
    <el-dialog v-model="data.dialogVisible" title="员工注册" width="90%" center>
      <el-form
        ref="ruleFormRef"
        :model="employeeRef"
        :rules="rules"
        label-position="top"
      >
        <el-form-item label="员工姓名:" prop="name">
          <el-input
            v-model="employeeRef.name"
            autocomplete="off"
            size="large"
          />
        </el-form-item>
        <el-form-item label="选择工作方式:" size="large" prop="modeId">
          <el-select v-model="employeeRef.modeId" placeholder="请选择工作类型">
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
          <el-button @click="data.dialogVisible = false" size="large"
            >取消</el-button
          >
          <el-button
            type="primary"
            :loading="data.isloading"
            @click="submitForm(ruleFormRef)"
            size="large"
            >确认
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
    <el-dialog
      v-model="data.addWorkVisible"
      title="添加工作记录"
      width="90%"
      center
    >
      <el-form
        ref="addWorkFormRef"
        :model="addWorkRef"
        :rules="addWorkRules"
        label-position="top"
      >
        <el-form-item label="员工姓名:" size="large" prop="employeeId">
          <el-select
            v-model="addWorkRef.employeeId"
            placeholder="请选择员工姓名"
            @change="handleEmployeeChange"
          >
            <el-option
              v-for="item in data.employeeList"
              :key="item.value"
              :label="item.text"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择厂名:" size="large" prop="factoryId">
          <el-select
            filterable
            v-model="addWorkRef.factoryId"
            placeholder="请选择厂名"
          >
            <el-option
              v-for="(item, index) in data.factoryList"
              :key="item.id"
              :label="item.factoryName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="床号：" prop="number">
          <el-input
            v-model="addWorkRef.number"
            autocomplete="off"
            size="large"
            placeholder="请输入床号"
            type="text"
          />
        </el-form-item>
        <el-form-item label="选择工作类型:" size="large" prop="categoryId">
          <el-select
            v-model="addWorkRef.categoryId"
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
            v-model="addWorkRef.styleNumber"
            autocomplete="off"
            size="large"
            placeholder="请输入款式编号"
            type="text"
          />
        </el-form-item>
        <el-form-item label="数量：" prop="quantity">
          <el-input
            v-model="addWorkRef.quantity"
            autocomplete="off"
            size="large"
            placeholder="请输入数量"
            type="text"
          />
        </el-form-item>
        <el-form-item label="工作日期:" size="large">
          <el-date-picker
            v-model="addWorkRef.createdTime"
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
          <el-button
            type="primary"
            :loading="data.isloading"
            @click="addWorkSubmitForm(addWorkFormRef)"
            size="large"
          >
            确认
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, onMounted, ref } from 'vue'
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
import { ElMessage } from 'element-plus'
import { useTransition } from '@vueuse/core'

// 进入页面时执行
onMounted(() => {
  queryEmployeeListHandle()
  queryJobListByEmployeeIdAndDateHandle()
})

const data = reactive({
  dialogVisible: false,
  addWorkVisible: false,
  SalaryVisible: false,
  isShowDate: false,
  id: Number(),
  startDate: '',
  endDate: '',
  date: '',
  employeeList: [],
  tableData: [],
  isloading: false,
  factoryList: [],
  categoryList: [],
  modeList: [],
  total: 0,
  currentPage: 1,
  pageSize: 10
})

// 查询员工列表
const queryEmployeeListHandle = async () => {
  const { data: res } = await queryEmployees()
  data.employeeList = res.data
  data.employeeList = data.employeeList.map(item => ({
    text: item.name, // Vant 组件显示的文字
    value: item.id, // 绑定到 v-model 的值
    modeId: item.modeId
  }))
  data.employeeList.unshift({ text: '选择员工', value: 0 })
}

// 获取当前选中员工的modeId
const handleEmployeeChange = employeeId => {
  const selectedEmployee = data.employeeList.find(
    item => item.value === employeeId
  )
  addWorkRef.modeId = selectedEmployee ? selectedEmployee.modeId : ''
}

// 按员工ID和日期区间查询员工的工作信息列表
const queryJobListByEmployeeIdAndDateHandle = async () => {
  const { data: res } = await queryJobListByEmployeeIdAndDate(
    data.id,
    data.startDate,
    data.endDate,
    data.currentPage,
    data.pageSize
  )
  data.tableData = res.data.records
  data.total = res.data.total
}

// 注册处理函数
const registerHandle = async () => {
  data.isloading = true
  const { data: res } = await saveEmployee(employeeRef)
  if (res.status === 200) {
    ElMessage.success(res.message)
  } else {
    ElMessage.error(res.message)
  }
  data.isloading = false
}

// 注册员工按钮点击事件
const clickAddEmployee = () => {
  data.dialogVisible = true
  queryModeListHandle()
}

// 添加工作信息按钮点击事件
const clickAddWork = () => {
  data.addWorkVisible = true
  queryFactoryListHandle()
  queryCategoryListHandle()
}

// 计算工资按钮点击事件
const clickSalary = () => {
  data.SalaryVisible = true
}

// 保存工作信息
const saveJobInfoHandle = async () => {
  const { data: res } = await saveJobInfo(addWorkRef)
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

// 删除工作信息
const deleteJobInfoByIdHandler = async id => {
  const { data: res } = await deleteJobInfoById(id)
  if (res.status === 200) {
    ElMessage.success(res.message)
  } else {
    ElMessage.error(res.message)
  }
  await queryJobListByEmployeeIdAndDateHandle()
}

// 查询厂名列表
const queryFactoryListHandle = async () => {
  const { data: res } = await queryFactoryList()
  data.factoryList = res.data
}

// 查询工作类型列表
const queryCategoryListHandle = async () => {
  const { data: res } = await queryCategoryList()
  data.categoryList = res.data
}
// 查询工作方式列表
const queryModeListHandle = async () => {
  const { data: res } = await queryModeList()
  data.modeList = res.data
}

const ruleFormRef = ref()
const addWorkFormRef = ref()
const querySalaryFormRef = ref()

const employeeRef = reactive({
  name: '',
  modeId: ''
})

// 添加工作信息表单参数
const addWorkRef = reactive({
  employeeId: '',
  factoryId: '',
  categoryId: '',
  modeId: '',
  number: '',
  styleNumber: '',
  quantity: '',
  createdTime: ''
})

// 重置按钮
const resetQueryConditionForm = () => {
  data.startDate = ''
  data.endDate = ''
  data.id = 0
  queryJobListByEmployeeIdAndDateHandle()
}

// 清除添加工作信息表单填写的参数
const resetSaveJobInfoForm = () => {
  addWorkFormRef.value.resetFields()
  data.addWorkVisible = false
}

const SalaryRef = reactive({
  employeeId: '',
  startDate: '',
  endDate: ''
})

const salaryTotal = ref(0)

// 计算工资请求
const salaryInquiry = async () => {
  const { data: res } = await querySalaryByCondition(
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
// 动画过渡
const outputValue = useTransition(salaryTotal, {
  duration: 1000
})

// 注册表单校验
const submitForm = async formEl => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      // 发起注册请求
      registerHandle()
    } else {
      console.log('error submit!', fields)
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
      console.log('error submit!', fields)
      data.addWorkVisible = false
    }
  })
}

// 查询计算工资表单校验
const querySalarySubmitForm = async formEl => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      salaryInquiry()
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
  console.log(data.startDate, data.endDate)
}

// 注册表单校验规则
const rules = reactive({
  name: [
    { required: true, message: '请输入员工姓名', trigger: 'blur' },
    { min: 2, max: 30, message: '长度在 2 到 6 个字符', trigger: 'blur' }
  ],
  modeId: [{ required: true, message: '请选择工作方式', trigger: 'blur' }]
})

const onlyNumberRule = {
  pattern: /^[0-9]+$/,
  message: '只能输入数字',
  trigger: 'blur'
}
// 添加工作记录表单校验规则
const addWorkRules = reactive({
  employeeId: [
    { required: true, message: '请选择员工姓名', trigger: 'blur' },
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
  ],
  factoryId: [{ required: true, message: '请选择厂名', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择工作类型', trigger: 'blur' }],
  number: [{ required: true, message: '请输入床号', trigger: 'blur' }],
  styleNumber: [
    { required: true, message: '请输入款式编号', trigger: 'blur' },
    onlyNumberRule
  ],
  quantity: [
    { required: true, message: '请输入数量', trigger: 'blur' },
    onlyNumberRule
  ],
  createdTime: [{ required: true, message: '请选择工作日期', trigger: 'blur' }]
})

// 计算工资表单校验规则
const querySalaryRule = reactive({
  employeeId: [
    { required: true, message: '请选择员工姓名', trigger: 'blur' },
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

/* .nav-container {
  position: fixed;
  top: 0;
  z-index: 100;
  width: 100%;
  box-sizing: border-box;
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-start;
  align-items: center;
  gap: 10px;
  padding: 15px;
  background: white;
}

.list-container {
  position: fixed;
  top: 130px;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 0 15px 15px 15px;
  overflow: auto;
  background: white; 
} */
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
  flex: auto;
}
.page-help {
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  height: 50px;
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
