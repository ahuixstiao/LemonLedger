<template>
  <div ref="tableSectionRef" class="home-work-table-section">
    <el-table
      :data="tableData"
      class="home-work-table"
      style="width: 100%"
      :max-height="tableMaxHeight"
      table-layout="fixed"
      stripe
      fit
      highlight-current-row
      show-summary
      :summary-method="summaryQuantityAndSalary"
    >
      <el-table-column sortable prop="name" label="员工名称" align="center" show-overflow-tooltip />
      <el-table-column sortable prop="factoryName" label="厂名" align="center" show-overflow-tooltip />
      <el-table-column sortable prop="number" label="床号" align="center" show-overflow-tooltip />
      <el-table-column sortable prop="styleNumber" label="款式编号" align="center" show-overflow-tooltip />
      <el-table-column sortable prop="category" label="类型" align="center" show-overflow-tooltip />
      <el-table-column prop="quantity" label="数量" align="center" />
      <el-table-column prop="salary" label="本床工资(单位: 元)" align="center" />
      <el-table-column sortable prop="createdDate" label="日期" align="center" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="120">
        <template #default="scope">
          <el-button link size="small" @click="$emit('edit', scope.row)">修改</el-button>
          <span class="op-divider">|</span>
          <el-button link type="danger" size="small" @click="$emit('delete', scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="home-page">
      <el-pagination
        background
        :total="total"
        :current-page="currentPage"
        :page-size="pageSize"
        :page-sizes="[5, 10, 20, 50, 100]"
        layout="sizes, prev, pager, next, jumper, ->"
        @update:current-page="val => $emit('update:currentPage', val)"
        @update:page-size="val => $emit('update:pageSize', val)"
        @current-change="$emit('query')"
        @size-change="$emit('query')"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUpdated, onBeforeUnmount } from 'vue'

defineProps({
  tableData: { type: Array, default: () => [] },
  total: { type: Number, default: 0 },
  currentPage: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 }
})

defineEmits(['edit', 'delete', 'query', 'update:currentPage', 'update:pageSize'])

const tableSectionRef = ref(null)
const tableMaxHeight = ref(360)

const calcTableMaxHeight = () => {
  const sectionEl = tableSectionRef.value
  if (!sectionEl) return
  const sectionHeight = sectionEl.clientHeight
  const pagerEl = sectionEl.querySelector('.home-page')
  const pagerHeight = pagerEl ? pagerEl.offsetHeight : 0
  tableMaxHeight.value = Math.max(180, Math.floor(sectionHeight - pagerHeight))
}

onMounted(() => {
  calcTableMaxHeight()
  window.addEventListener('resize', calcTableMaxHeight)
})

onUpdated(() => {
  calcTableMaxHeight()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', calcTableMaxHeight)
})

const summaryQuantityAndSalary = ({ columns, data }) => {
  const sums = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    if (['quantity', 'salary'].includes(column.property)) {
      const total = data.reduce((sum, row) => {
        const value = Number(row[column.property])
        return !isNaN(value) ? sum + value : sum
      }, 0)
      sums[index] = column.property === 'salary' ? total.toFixed(2) : total
    } else {
      sums[index] = ''
    }
  })
  return sums
}
</script>

<style scoped>
.home-work-table-section {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 10px;
  border: 1px solid var(--border-soft);
  border-radius: var(--radius-md);
  background: #fff;
}

.home-work-table {
  flex: 1;
  min-height: 0;
}

:deep(.home-work-table .cell) {
  white-space: nowrap;
}

.home-page {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 12px 0 4px;
  flex-shrink: 0;
}

:deep(.el-pagination) {
  flex-wrap: wrap;
  justify-content: center;
  row-gap: 8px;
}

@media (max-width: 768px) {
  .home-work-table-section {
    padding: 8px;
    overflow: visible;
  }

  .home-page {
    justify-content: center;
    padding: 10px 0 4px;
  }
}
</style>
