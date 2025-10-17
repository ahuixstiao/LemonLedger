<template>
  <div class="home-container">
    <div class="nav-container">
      <van-button type="primary" @click="data.dialogVisible = true"
        >注册</van-button
      >
      <van-button type="primary" @click="clickAddWork">添加工作记录</van-button>
      <van-button type="primary" @click="clickSalary">汇总薪资</van-button>
      <van-cell
        title="选择日期区间"
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
      <van-dropdown-menu>
        <van-dropdown-item
          v-model="data.id"
          @change="queryJobListByIDAndDateHandle"
          :options="data.joblist"
        />
      </van-dropdown-menu>
      <van-button type="primary" @click="resetForm">重置</van-button>
    </div>
    <div class="list-container">
      <el-table :data="data.tableData" height="60" stripe style="height: 100%">
        <el-table-column prop="name" label="员工名称" />
        <el-table-column prop="factoryName" label="厂名" />
        <el-table-column prop="number" label="床号" />
        <el-table-column prop="styleNumber" label="款式编号" />
      </el-table>
    </div>
    <el-dialog v-model="data.dialogVisible" title="员工注册" width="90%" center>
      <el-form
        ref="ruleFormRef"
        :model="jobRef"
        :rules="rules"
        label-position="top"
      >
        <el-form-item label="员工姓名：" prop="name">
          <el-input v-model="jobRef.name" autocomplete="off" size="large" />
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
          >
            确认
          </el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog v-model="data.SalaryVisible" title="薪资汇总" width="90%" center>
      <el-form label-position="top">
        <el-form-item label="员工姓名:" size="large">
          <el-select v-model="SalaryRef.name" placeholder="请选择员工">
            <el-option
              v-for="(item, index) in data.joblist"
              :label="item.text"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时间日期:" size="large">
          <el-date-picker
            v-model="data.salaryStartDate"
            type="date"
            placeholder="开始日期"
          />
          <el-date-picker
            v-model="data.salaryEndDate"
            type="date"
            placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item size="large">
          <el-button
            type="primary"
            style="width: 100%"
            size="large"
            @click="salaryInquiry()"
          >
            查询薪资</el-button
          >
        </el-form-item>
        <el-form-item size="large">
          <el-statistic
            title="薪资总和"
            Transactions
            :value="outputValue"
            style="text-align: center; margin: 0 auto; display: block"
          />
        </el-form-item>
      </el-form>
    </el-dialog>
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
        <el-form-item label="员工姓名:" size="large" prop="name">
          <el-select v-model="addWorkRef.name" placeholder="请选择员工姓名">
            <el-option
              v-for="item in data.joblist"
              :label="item.text"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择厂名:" size="large" prop="id">
          <el-select v-model="addWorkRef.id" placeholder="请选择厂名">
            <el-option
              v-for="(item, index) in data.factoryList"
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
            type="number"
          />
        </el-form-item>
        <el-form-item label="款式编号：" prop="styleNumber">
          <el-input
            v-model="addWorkRef.styleNumber"
            autocomplete="off"
            size="large"
            placeholder="请输入款式编号"
            type="number"
          />
        </el-form-item>
        <el-form-item label="数量：" prop="quantity">
          <el-input
            v-model="addWorkRef.quantity"
            autocomplete="off"
            size="large"
            placeholder="请输入数量"
            type="number"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.addWorkVisible = false" size="large"
            >取消</el-button
          >
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
  saveJobInfo,
  queryJobListByIDAndDate,
  queryFactoryList
} from '../nwtwork/index.js'
import { ElMessage } from 'element-plus'
import { useTransition } from '@vueuse/core'
onMounted(() => {
  queryEmployeesHandle()
  queryJobListByIDAndDateHandle()
})

// 按员工id查询员工列表
const queryEmployeesHandle = async () => {
  const { data: res } = await queryEmployees()
  data.joblist = res.data
  data.joblist = data.joblist.map(item => ({
    text: item.name, // Vant 组件显示的文字
    value: item.id // 绑定到 v-model 的值
  }))
  data.joblist.unshift({ text: '员工名字', value: 0 })
}

// 重置按钮
const resetForm = () => {
  data.startDate = ''
  data.endDate = ''
  data.id = 0
  queryJobListByIDAndDateHandle()
}

// 按区间/id查询处理函数
const queryJobListByIDAndDateHandle = async () => {
  const { data: res } = await queryJobListByIDAndDate(
    data.id,
    data.startDate,
    data.endDate
  )
  data.tableData = res.data.records
  console.log(data.tableData)
}

const data = reactive({
  dialogVisible: false,
  addWorkVisible: false,
  SalaryVisible: false,
  isShowDate: false,
  id: Number(),
  startDate: '',
  endDate: '',
  date: '',
  joblist: [],
  tableData: [],
  isloading: false,
  factoryList: [],
  salaryStartDate: '',
  salaryEndDate: ''
})

// 注册处理函数
const registerHandle = async () => {
  data.isloading = true
  const { data: res } = await saveJobInfo(jobRef)
  console.log(res)
  if (res.status === 200) {
    ElMessage.success(res.message)
  } else {
    ElMessage.error(res.message)
  }
  data.isloading = false
}

const clickAddWork = () => {
  data.addWorkVisible = true
  queryFactoryListHandle()
}
const clickSalary = () => {
  data.SalaryVisible = true
  //console.log(data.joblist)
}

// 查询厂名列表
const queryFactoryListHandle = async () => {
  const { data: res } = await queryFactoryList()
  data.factoryList = res.data.records
  console.log(data.factoryList)
}

const ruleFormRef = ref()
const addWorkFormRef = ref()

const jobRef = reactive({
  name: ''
})

const addWorkRef = reactive({
  name: '',
  id: '',
  number: '',
  styleNumber: '',
  quantity: ''
})

const SalaryRef = reactive({
  name: '',
  startDate: '',
  endDate: ''
})

// 注册表单校验规则
const rules = reactive({
  name: [
    { required: true, message: '请输入员工姓名', trigger: 'blur' },
    { min: 2, max: 30, message: '长度在 2 到 6 个字符', trigger: 'blur' }
  ]
})

// 添加工作记录表单校验规则
const addWorkRules = reactive({
  name: [{ required: true, message: '请选择员工姓名', trigger: 'blur' }],
  id: [{ required: true, message: '请选择厂名', trigger: 'blur' }],
  number: [
    { required: true, message: '请输入床号', trigger: 'blur' },
    { pattern: /^[0-9]+$/, message: '只能输入数字', trigger: 'blur' }
  ],
  styleNumber: [
    { required: true, message: '请输入款式编号', trigger: 'blur' },
    { pattern: /^[0-9]+$/, message: '只能输入数字', trigger: 'blur' }
  ],
  quantity: [
    { required: true, message: '请输入数量', trigger: 'blur' },
    { pattern: /^[0-9]+$/, message: '只能输入数字', trigger: 'blur' }
  ]
})

const salaryTotal = ref(0)

// 查询薪资
const salaryInquiry = () => {
  salaryTotal.value = 172000
}
const outputValue = useTransition(salaryTotal, {
  duration: 200
})
// 动画过渡

// 注册表单校验
const submitForm = async formEl => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      // 发起注册请求
      registerHandle()
      console.log('submit!')
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
      console.log('submit!')
    } else {
      console.log('error submit!', fields)
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
  queryJobListByIDAndDateHandle()
  console.log(data.startDate, data.endDate)
}
</script>

<style scoped>
.home-contain {
  height: 100%;
}
.nav-container {
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
  top: 130px; /* 适配 nav-container 高度 */
  left: 0;
  right: 0;
  bottom: 0;
  padding: 0 15px 15px 15px;
  overflow: auto;
  background: white; /* 避免内容重叠 */
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
</style>
