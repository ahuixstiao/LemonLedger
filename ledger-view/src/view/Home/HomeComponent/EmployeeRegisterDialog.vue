<template>
  <el-dialog
    :model-value="visible"
    title="员工注册"
    width="90%"
    center
    @update:model-value="val => $emit('update:visible', val)"
  >
    <el-form ref="ruleFormRef" :model="form" :rules="rules" label-position="top">
      <el-form-item label="员工姓名:" prop="name">
        <el-input v-model="form.name" autocomplete="off" size="large" />
      </el-form-item>
      <el-form-item label="选择工作方式:" size="large" prop="modeId">
        <el-select v-model="form.modeId" placeholder="请选择工作类型">
          <el-option v-for="item in modeList" :key="item.id" :label="item.mode" :value="item.id" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="$emit('update:visible', false)" size="large">取消</el-button>
        <el-button type="primary" :loading="loading" @click="submit" size="large">确认</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref } from 'vue'

defineProps({
  visible: { type: Boolean, default: false },
  modeList: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false }
})

const emit = defineEmits(['update:visible', 'submit'])

const ruleFormRef = ref()

const form = reactive({
  name: '',
  modeId: ''
})

const rules = reactive({
  name: [
    { required: true, message: '请输入员工姓名', trigger: 'blur' },
    { min: 2, max: 30, message: '长度在 2 到 6 个字符', trigger: 'blur' }
  ],
  modeId: [{ required: true, message: '请选择工作方式', trigger: 'blur' }]
})

const submit = async () => {
  if (!ruleFormRef.value) return
  await ruleFormRef.value.validate(valid => {
    if (valid) {
      emit('submit', { ...form })
    }
  })
}
</script>

<style scoped>
:deep(.el-form-item__label) {
  font-size: 16px;
}
</style>
