<template>
  <div class="common-filter-bar toolbar">
    <el-button
      v-for="action in actions"
      :key="action.key"
      :type="action.type || 'default'"
      @click="emit('action', action.key)"
    >
      {{ action.label }}
    </el-button>

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

    <el-button v-if="showReset" type="primary" @click="handleReset">{{ resetText }}</el-button>
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
  flex-wrap: nowrap;
  gap: var(--layout-gap-sm);
  flex-shrink: 0;
  padding: var(--section-padding-sm);
  overflow-x: hidden;
}

.common-filter-bar > * {
  flex: 1 1 0;
  min-width: 0;
}

.common-filter-bar :deep(.el-button),
.common-filter-bar :deep(.el-select),
.common-filter-bar :deep(.el-input),
.common-filter-bar :deep(.el-date-editor) {
  width: 100%;
}

@media (max-width: 768px) {
  .common-filter-bar {
    display: grid;
    grid-template-columns: minmax(0, 1fr);
    gap: var(--layout-gap-sm);
    padding: 0;
    overflow-x: visible;
  }

  .common-filter-bar > * {
    min-width: 0;
    width: 100%;
  }

  .common-filter-bar :deep(.el-button),
  .common-filter-bar :deep(.el-select),
  .common-filter-bar :deep(.el-input),
  .common-filter-bar :deep(.el-date-editor) {
    width: 100%;
    margin: 0;
  }
}
</style>
