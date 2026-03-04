<template>
  <el-dialog :model-value="visible" title="工资计算" width="90%" center @update:model-value="val => $emit('update:visible', val)">
    <el-form ref="querySalaryFormRef" label-position="top" :model="form" :rules="rules">
      <el-form-item label="员工姓名:" size="large" prop="employeeId">
        <el-select v-model="form.employeeId" placeholder="请选择员工">
          <el-option v-for="item in employeeList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="计算日期:" size="large">
        <el-date-picker v-model="form.startDate" type="date" format="YYYY/MM/DD" value-format="YYYY-MM-DD" placeholder="开始日期" />
        <el-date-picker v-model="form.endDate" type="date" format="YYYY/MM/DD" value-format="YYYY-MM-DD" placeholder="结束日期" />
      </el-form-item>
      <el-form-item size="large">
        <el-button type="primary" style="width: 100%" size="large" @click="submit">计算</el-button>
      </el-form-item>
      <el-form-item size="large">
        <el-statistic title="工资总和" :value="outputValue" :precision="2" style="text-align: center; margin: 0 auto; display: block" />
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useTransition } from '@vueuse/core'

const props = defineProps({
  visible: { type: Boolean, default: false },
  employeeList: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:visible', 'submit'])

const salaryTotal = ref(0)
const outputValue = useTransition(salaryTotal, { duration: 1000 })

const form = reactive({
  employeeId: '',
  startDate: '',
  endDate: ''
})

const querySalaryFormRef = ref()

const rules = reactive({
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

const submit = async () => {
  if (!querySalaryFormRef.value) return
  await querySalaryFormRef.value.validate(valid => {
    if (valid) {
      emit('submit', {
        ...form,
        onResult: total => {
          salaryTotal.value = Number(total || 0)
        }
      })
    }
  })
}
</script>

<style scoped>
:deep(.el-form-item__content) {
  flex-wrap: nowrap;
  gap: 10px;
}

:deep(.el-statistic__number::after) {
  content: '元';
}

:deep(.el-form-item__label) {
  font-size: 16px;
}
</style>
