<template>
  <div class="common-filter-bar toolbar">
    <div class="action-group">
      <el-button
        v-for="action in actions"
        :key="action.key"
        :type="action.type || 'default'"
        class="action-btn"
        @click="emit('action', action.key)"
      >
        {{ action.label }}
      </el-button>
    </div>

    <div class="field-group">
      <template v-for="field in fields" :key="field.key">
        <el-select
          v-if="field.type === 'select'"
          :model-value="modelValue[field.key]"
          :placeholder="field.placeholder"
          :filterable="field.filterable ?? false"
          :clearable="field.clearable ?? false"
          @update:model-value="val => updateField(field.key, val)"
        >
          <el-option
            v-for="option in field.options || []"
            :key="option[field.optionValue || 'value']"
            :label="option[field.optionLabel || 'label']"
            :value="option[field.optionValue || 'value']"
          />
        </el-select>

        <el-date-picker
          v-else-if="field.type === 'date'"
          :model-value="modelValue[field.key]"
          type="date"
          :format="field.format || 'YYYY/MM/DD'"
          :value-format="field.valueFormat || 'YYYY-MM-DD'"
          :placeholder="field.placeholder"
          :editable="field.editable ?? true"
          :clearable="field.clearable ?? false"
          :popper-class="field.popperClass || 'month-only-picker'"
          @update:model-value="val => updateField(field.key, val)"
        />

        <el-input
          v-else
          :model-value="modelValue[field.key]"
          :placeholder="field.placeholder"
          :clearable="field.clearable ?? false"
          @update:model-value="val => updateField(field.key, val)"
        />
      </template>
    </div>

    <el-button v-if="showReset" type="primary" class="reset-btn" @click="handleReset">{{ resetText }}</el-button>
  </div>
</template>

<script setup>
const props = defineProps({
  modelValue: { type: Object, required: true },
  fields: { type: Array, default: () => [] },
  actions: { type: Array, default: () => [] },
  initialValue: { type: Object, default: () => ({}) },
  showReset: { type: Boolean, default: true },
  resetText: { type: String, default: '重置' },
  queryOnChange: { type: Boolean, default: true }
})

const emit = defineEmits(['update:modelValue', 'change', 'query', 'reset', 'action'])

const updateField = (key, value) => {
  const nextModel = {
    ...props.modelValue,
    [key]: value ?? ''
  }
  emit('update:modelValue', nextModel)
  emit('change', { key, value: value ?? '', model: nextModel })
  if (props.queryOnChange) emit('query')
}

const handleReset = () => {
  const resetModel = {
    ...props.modelValue,
    ...props.initialValue
  }
  emit('update:modelValue', resetModel)
  emit('reset', resetModel)
}
</script>

<style scoped>
.common-filter-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  padding: 4px;
  border-radius: 10px;
}

.action-group,
.field-group {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.action-group {
  flex: 1 1 auto;
}

.field-group {
  flex: 4 1 0;
}

.action-group > *,
.field-group > *,
.reset-btn {
  min-width: 0;
  flex: 1 1 0;
}

.common-filter-bar :deep(.el-button),
.common-filter-bar :deep(.el-select),
.common-filter-bar :deep(.el-input),
.common-filter-bar :deep(.el-date-editor) {
  width: 100%;
}

.action-btn,
.reset-btn {
  min-height: 38px;
  font-weight: 600;
  border-radius: 9px;
}

.common-filter-bar :deep(.el-input__wrapper),
.common-filter-bar :deep(.el-select__wrapper),
.common-filter-bar :deep(.el-date-editor.el-input__wrapper) {
  border-radius: 9px;
  min-height: 38px;
}

@media (max-width: 768px) {
  .common-filter-bar {
    display: grid;
    grid-template-columns: minmax(0, 1fr);
    gap: 10px;
    padding: 0;
    justify-items: stretch;
  }

  .action-group,
  .field-group {
    display: grid;
    grid-template-columns: minmax(0, 1fr);
    gap: 8px;
    width: 100%;
  }

  .action-group > *,
  .field-group > *,
  .reset-btn {
    width: 100%;
  }

  .action-btn,
  .reset-btn {
    min-height: 40px;
    margin: 0;
  }

  .common-filter-bar :deep(.el-input__wrapper),
  .common-filter-bar :deep(.el-select__wrapper),
  .common-filter-bar :deep(.el-date-editor.el-input__wrapper) {
    min-height: 40px;
  }
}
</style>
