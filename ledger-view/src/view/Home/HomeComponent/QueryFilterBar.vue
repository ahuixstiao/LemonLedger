<template>
  <div class="nav-container">
    <el-button type="primary" @click="$emit('go-claim-work-order')">认领工单</el-button>
    <el-button type="primary" @click="$emit('salary')">计算工资</el-button>

    <el-date-picker
      :model-value="startDate"
      type="date"
      format="YYYY/MM/DD"
      value-format="YYYY-MM-DD"
      placeholder="开始日期"
      popper-class="month-only-picker"
      @change="onStartDateChange"
    />

    <el-date-picker
      :model-value="endDate"
      type="date"
      format="YYYY/MM/DD"
      value-format="YYYY-MM-DD"
      placeholder="结束日期"
      popper-class="month-only-picker"
      @change="onEndDateChange"
    />

    <el-select
      :model-value="employeeId"
      filterable
      placeholder="选择员工"
      @change="onEmployeeChange"
    >
      <el-option
        v-for="item in employeeOptions"
        :key="item.value"
        :label="item.text"
        :value="item.value"
      />
    </el-select>

    <el-select
      :model-value="categoryId"
      placeholder="选择工作类型"
      @change="onCategoryChange"
    >
      <el-option
        v-for="item in categoryOptions"
        :key="item.value"
        :label="item.text"
        :value="item.value"
      />
    </el-select>

    <el-select
      filterable
      :model-value="factoryId"
      placeholder="选择成衣厂"
      @change="onFactoryChange"
    >
      <el-option
        v-for="item in factoryList"
        :key="item.id"
        :label="item.factoryName"
        :value="item.id"
      />
    </el-select>

    <el-input
      :model-value="number"
      placeholder="要查询的床号"
      @input="onNumberInput"
    />

    <el-button @click="onReset">重置</el-button>
  </div>
</template>

<script setup>
const props = defineProps({
  startDate: { type: String, default: '' },
  endDate: { type: String, default: '' },
  employeeId: { type: Number, default: 0 },
  categoryId: { type: Number, default: 0 },
  factoryId: { type: [String, Number], default: '' },
  number: { type: String, default: '' },
  employeeOptions: { type: Array, default: () => [] },
  categoryOptions: { type: Array, default: () => [] },
  factoryList: { type: Array, default: () => [] }
})

const emit = defineEmits([
  'go-claim-work-order',
  'salary',
  'query',
  'reset',
  'update:employeeId',
  'update:categoryId',
  'update:factoryId',
  'update:number',
  'date-range-change'
])

const onStartDateChange = val => {
  emit('date-range-change', {
    startDate: val || '',
    endDate: props.endDate || ''
  })
}

const onEndDateChange = val => {
  emit('date-range-change', {
    startDate: props.startDate || '',
    endDate: val || ''
  })
}

const onEmployeeChange = val => {
  emit('update:employeeId', val)
  emit('query')
}

const onCategoryChange = val => {
  emit('update:categoryId', val)
  emit('query')
}

const onFactoryChange = val => {
  emit('update:factoryId', val)
  emit('query')
}

const onNumberInput = val => {
  emit('update:number', val)
  emit('query')
}

const onReset = () => {
  emit('reset')
}
</script>

<style scoped>
.nav-container {
  display: flex;
  align-items: center;
  flex-wrap: nowrap;
  gap: var(--layout-gap-lg);
  flex-shrink: 0;
  padding: var(--section-padding-sm);
  overflow-x: auto;
}

.nav-container > * {
  flex: 0 0 auto;
}

.nav-container > :nth-child(1),
.nav-container > :nth-child(2),
.nav-container > :nth-child(9) {
  width: 110px;
}

.nav-container > :nth-child(3),
.nav-container > :nth-child(4),
.nav-container > :nth-child(5),
.nav-container > :nth-child(6),
.nav-container > :nth-child(7),
.nav-container > :nth-child(8) {
  width: 150px;
}

:deep(.el-button),
:deep(.el-select),
:deep(.el-input),
:deep(.el-date-editor) {
  width: 100%;
}

@media (max-width: 768px) {
  .nav-container {
    display: grid;
    grid-template-columns: minmax(0, 1fr);
    gap: var(--layout-gap-sm);
    padding: 0;
    overflow-x: visible;
  }

  .nav-container > * {
    min-width: 0;
    width: 100%;
  }

  .nav-container :deep(.el-button),
  .nav-container :deep(.el-select),
  .nav-container :deep(.el-input),
  .nav-container :deep(.el-date-editor) {
    width: 100%;
    margin: 0;
  }
}
</style>
