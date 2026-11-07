<template>
  <div class="filter-bar front-filter-bar">
    <template v-for="field in fields" :key="fieldKey(field)">
      <!-- 起止日期成组出现，换行时不会被拆到两行 -->
      <div v-if="field.type === 'date-range'" class="filter-dates">
        <el-date-picker
          :model-value="modelValue[field.keys[0]]"
          type="date"
          format="YYYY/MM/DD"
          value-format="YYYY-MM-DD"
          :placeholder="field.placeholders?.[0] || '开始日期'"
          :editable="field.editable ?? false"
          :clearable="true"
          :popper-class="field.popperClass || 'month-only-picker'"
          @update:model-value="val => updateField(field.keys[0], val)"
        />
        <span class="date-sep">→</span>
        <el-date-picker
          :model-value="modelValue[field.keys[1]]"
          type="date"
          format="YYYY/MM/DD"
          value-format="YYYY-MM-DD"
          :placeholder="field.placeholders?.[1] || '结束日期'"
          :editable="field.editable ?? false"
          :clearable="true"
          :popper-class="field.popperClass || 'month-only-picker'"
          @update:model-value="val => updateField(field.keys[1], val)"
        />
      </div>

      <el-select
        v-else-if="field.type === 'select'"
        :model-value="modelValue[field.key]"
        :placeholder="field.placeholder"
        :filterable="field.filterable ?? false"
        :clearable="field.clearable ?? false"
        :class="{ 'filter-wide': field.wide }"
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
        :class="{ 'filter-wide': field.wide }"
        @update:model-value="val => updateField(field.key, val)"
      />
    </template>

    <div class="filter-actions">
      <el-button type="primary" @click="emit('query')">{{ queryText }}</el-button>
      <el-button v-if="showReset" @click="handleReset">{{ resetText }}</el-button>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  modelValue: { type: Object, required: true },
  fields: { type: Array, default: () => [] },
  initialValue: { type: Object, default: () => ({}) },
  showReset: { type: Boolean, default: true },
  queryText: { type: String, default: '查询' },
  resetText: { type: String, default: '重置' }
})

const emit = defineEmits(['update:modelValue', 'change', 'query', 'reset'])

const fieldKey = field => field.key || (field.keys || []).join('-')

const updateField = (key, value) => {
  const nextModel = {
    ...props.modelValue,
    [key]: value ?? ''
  }
  emit('update:modelValue', nextModel)
  emit('change', { key, value: value ?? '', model: nextModel })
}

const handleReset = () => {
  const resetModel = {
    ...props.modelValue,
    ...props.initialValue
  }
  emit('update:modelValue', resetModel)
  emit('reset', resetModel)
  emit('query')
}
</script>

<style scoped>
.front-filter-bar {
  row-gap: 10px;
}

.date-sep {
  color: var(--admin-text-muted);
  font-size: 12px;
}

.front-filter-bar :deep(.el-select),
.front-filter-bar :deep(.el-input),
.front-filter-bar :deep(.el-date-editor) {
  flex: 0 0 auto;
}

@media (max-width: 960px) {
  .front-filter-bar {
    display: grid;
    grid-template-columns: minmax(0, 1fr);
    gap: 10px;
  }

  .front-filter-bar :deep(.el-select),
  .front-filter-bar :deep(.el-input),
  .front-filter-bar :deep(.el-date-editor) {
    width: 100%;
  }

  .filter-dates {
    display: grid;
    grid-template-columns: minmax(0, 1fr) auto minmax(0, 1fr);
    gap: 8px;
    align-items: center;
  }

  .filter-actions {
    margin-left: 0;
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px;
  }

  .filter-actions :deep(.el-button) {
    width: 100%;
    margin-left: 0;
  }
}
</style>
