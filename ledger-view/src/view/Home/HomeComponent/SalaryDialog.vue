<template>
  <el-dialog
    :model-value="visible"
    title="工资计算"
    width="420px"
    class="app-dialog"
    @update:model-value="val => $emit('update:visible', val)"
  >
    <el-form ref="querySalaryFormRef" label-position="top" :model="form" :rules="rules" class="salary-form">
      <el-form-item label="员工姓名" prop="employeeId">
        <el-select v-model="form.employeeId" placeholder="请选择员工">
          <el-option v-for="item in employeeList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="计算日期" class="date-range-item">
        <el-date-picker v-model="form.startDate" type="date" format="YYYY/MM/DD" value-format="YYYY-MM-DD" placeholder="开始日期" />
        <el-date-picker v-model="form.endDate" type="date" format="YYYY/MM/DD" value-format="YYYY-MM-DD" placeholder="结束日期" />
      </el-form-item>
      <div class="salary-result">
        <span class="salary-result__label">工资总和</span>
        <span class="salary-result__value">{{ salaryOutput }}</span>
      </div>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="$emit('update:visible', false)">关闭</el-button>
        <el-button type="primary" @click="submit">计算</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useTransition } from '@vueuse/core'
import { formatMoney } from '../../Admin/adminShared.js'

const props = defineProps({
  visible: { type: Boolean, default: false },
  employeeList: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:visible', 'submit'])

const salaryTotal = ref(0)
const outputValue = useTransition(salaryTotal, { duration: 1000 })
const salaryOutput = computed(() => formatMoney(Number(outputValue.value) || 0))

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

watch(
  () => props.visible,
  visible => {
    if (!visible) salaryTotal.value = 0
  }
)

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
.salary-form {
  padding: 2px;
}

.date-range-item :deep(.el-form-item__content) {
  flex-wrap: nowrap;
  gap: 8px;
}

.date-range-item :deep(.el-date-editor) {
  flex: 1 1 0;
  width: auto;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.dialog-footer :deep(.el-button + .el-button) {
  margin-left: 0;
}

.salary-result {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-top: 2px;
  padding: 14px 16px;
  border-radius: 12px;
  background: #f4f7fe;
  border: 1px solid var(--admin-card-border);
}

.salary-result__label {
  font-size: 13px;
  color: var(--admin-text-secondary);
}

.salary-result__value {
  font-size: 22px;
  font-weight: 700;
  color: var(--brand-1);
  font-variant-numeric: tabular-nums;
}

@media (max-width: 960px) {
  .date-range-item :deep(.el-form-item__content) {
    flex-direction: column;
    align-items: stretch;
  }

  .date-range-item :deep(.el-date-editor) {
    width: 100%;
  }
}
</style>
