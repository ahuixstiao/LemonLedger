<template>
  <div class="home-container">
    <div class="nav-container">
      <van-button type="primary" @click="data.dialogVisible = true"
        >注册</van-button
      >
      <van-button type="primary">添加工作记录</van-button>
      <van-button type="primary">汇总薪资</van-button>
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
        @confirm="onConfirm()"
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
  </div>
</template>

<script setup>
import { reactive, onMounted, ref } from 'vue'
import {
  queryEmployees,
  saveJobInfo,
  queryJobListByIDAndDate
} from '../nwtwork/index.js'
import { ElMessage } from 'element-plus'
onMounted(() => {
  queryEmployeesHandle()
  queryJobListByIDAndDateHandle()
})

const queryEmployeesHandle = async () => {
  const { data: res } = await queryEmployees()
  data.joblist = res.data
  data.joblist = data.joblist.map(item => ({
    text: item.name, // Vant 组件显示的文字
    value: item.id // 绑定到 v-model 的值
  }))
  data.joblist.unshift({ text: '员工名字', value: 0 })
}

const resetForm = () => {
  data.startDate = ''
  data.endDate = ''
  data.id = 0
  queryJobListByIDAndDateHandle()
}

const queryJobListByIDAndDateHandle = async () => {
  const { data: res } = await queryJobListByIDAndDate(
    data.id,
    data.startDate,
    data.endDate
  )
  data.tableData = res.data.records
  console.log(res.data)
}
const data = reactive({
  dialogVisible: false,
  isShowDate: false,
  id: Number(),
  startDate: '',
  endDate: '',
  date: '',
  joblist: [],
  tableData: [
    {
      jobName: '王小明',
      factoryName: 'A厂',
      bedNumber: '1',
      styleNumber: 'A1001'
    },
    {
      jobName: '张小红',
      factoryName: 'B厂',
      bedNumber: '2',
      styleNumber: 'B1001'
    },
    {
      jobName: '李小刚',
      factoryName: 'C厂',
      bedNumber: '3',
      styleNumber: 'C1001'
    },
    {
      jobName: '赵小丽',
      factoryName: 'D厂',
      bedNumber: '4',
      styleNumber: 'D1001'
    }
  ],
  isloading: false
})

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

const ruleFormRef = ref()
const jobRef = reactive({
  name: ''
})
const rules = reactive({
  name: [
    { required: true, message: '请输入员工姓名', trigger: 'blur' },
    { min: 2, max: 30, message: '长度在 2 到 6 个字符', trigger: 'blur' }
  ]
})

const submitForm = async formEl => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      registerHandle()
      console.log('submit!')
    } else {
      console.log('error submit!', fields)
    }
  })
}

const formatDate = date => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}
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
</style>
