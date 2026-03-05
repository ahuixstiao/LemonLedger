<template>
  <el-dialog
    :model-value="visible"
    :title="dialogMode === 0 ? '添加工作记录' : dialogMode === 1 ? '修改工作记录' : ''"
    width="90%"
    center
    @update:model-value="val => $emit('update:visible', val)"
  >
    <el-form ref="jobInfoFormRef" :model="jobInfo" :rules="jobRules" label-position="top">
      <el-form-item label="员工姓名:" size="large" prop="employeeId">
        <el-select
          filterable
          v-model="jobInfo.employeeId"
          placeholder="请选择员工姓名"
          @change="val => $emit('employee-change', val)"
        >
          <el-option v-for="item in employeeList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="选择厂名:" size="large" prop="factoryId">
        <el-select filterable v-model="jobInfo.factoryId" placeholder="请选择厂名">
          <el-option v-for="item in factoryList" :key="item.id" :label="item.factoryName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="床号：" prop="number">
        <el-input v-model="jobInfo.number" autocomplete="off" size="large" placeholder="请输入床号" type="text" />
      </el-form-item>
      <el-form-item label="选择工作类型:" size="large" prop="categoryId">
        <el-select v-model="jobInfo.categoryId" placeholder="请选择工作类型">
          <el-option v-for="item in categoryList" :key="item.id" :label="item.category" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="款式编号：" prop="styleNumber">
        <el-input v-model="jobInfo.styleNumber" autocomplete="off" size="large" placeholder="请输入款式编号" type="text" />
      </el-form-item>
      <el-form-item label="数量：" prop="quantity">
        <el-input v-model="jobInfo.quantity" autocomplete="off" size="large" placeholder="请输入数量" type="text" />
      </el-form-item>
      <el-form-item label="工作日期:" size="large" prop="createdDate">
        <el-date-picker
          :editable="false"
          v-model="jobInfo.createdDate"
          type="date"
          format="YYYY/MM/DD"
          value-format="YYYY-MM-DD"
          placeholder="工作记录日期"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="default" size="large" @click="handleCancel">取消</el-button>
        <el-button type="primary" size="large" :loading="loading" @click="handleSubmit">
          {{ dialogMode === 0 ? '新增' : '修改' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: { type: Boolean, default: false },
  dialogMode: { type: Number, default: 0 },
  loading: { type: Boolean, default: false },
  jobInfo: { type: Object, required: true },
  employeeList: { type: Array, default: () => [] },
  factoryList: { type: Array, default: () => [] },
  categoryList: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:visible', 'submit', 'cancel', 'employee-change'])

const jobInfoFormRef = ref()

const onlyNumberRule = { pattern: /^[0-9]+$/, message: '只能输入数字', trigger: 'blur' }

const jobRules = reactive({
  employeeId: [{ required: true, message: '请选择员工姓名', trigger: 'blur' }],
  factoryId: [{ required: true, message: '请选择厂名', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择工作类型', trigger: 'blur' }],
  number: [{ required: true, message: '请输入床号', trigger: 'blur' }],
  styleNumber: [{ required: true, message: '请输入款式编号', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }, onlyNumberRule],
  createdDate: [{ required: true, message: '请选择工作日期', trigger: 'blur' }]
})

const handleSubmit = async () => {
  if (!jobInfoFormRef.value) return
  await jobInfoFormRef.value.validate(valid => {
    if (valid) {
      emit('submit')
    } else {
      ElMessage.error('请检查是否填写正确')
    }
  })
}

const handleCancel = () => {
  jobInfoFormRef.value?.clearValidate()
  emit('cancel')
  emit('update:visible', false)
}
</script>
