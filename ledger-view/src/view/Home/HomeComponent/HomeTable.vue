<template>
  <div class="front-table-wrap">
    <el-table
      :data="tableData"
      class="front-table"
      height="100%"
      table-layout="fixed"
      stripe
      fit
      highlight-current-row
      empty-text="还没有工单记录"
      show-summary
      :summary-method="getSummaries"
    >
      <el-table-column prop="name" label="员工" align="center" min-width="180" sortable show-overflow-tooltip>
        <template #default="scope">
          <span class="entity-cell">
            <span class="entity-avatar">{{ avatarText(scope.row.name) }}</span>
            <span class="entity-name">{{ scope.row.name }}</span>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="factoryName" label="厂名" align="center" min-width="190" sortable show-overflow-tooltip />
      <el-table-column prop="number" label="床号" align="center" min-width="110" sortable show-overflow-tooltip />
      <el-table-column prop="styleNumber" label="款式编号" align="center" min-width="160" sortable show-overflow-tooltip />
      <el-table-column prop="category" label="类型" align="center" min-width="110" sortable show-overflow-tooltip>
        <template #default="scope">
          <el-tag size="small" effect="plain" type="info">{{ scope.row.category }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" align="center" min-width="110">
        <template #default="scope">
          <span class="num">{{ formatNumber(scope.row.quantity) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="salary" label="本床工资" align="center" min-width="150">
        <template #default="scope">
          <span class="money">{{ formatMoney(scope.row.salary) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdDate" label="日期" align="center" min-width="140" sortable show-overflow-tooltip>
        <template #default="scope">
          <span class="date-cell">{{ formatDate(scope.row.createdDate) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150" fixed="right">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="$emit('edit', scope.row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="$emit('delete', scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager-row front-pager">
      <el-pagination
        background
        :total="total"
        :current-page="currentPage"
        :page-size="pageSize"
        :page-sizes="[5, 10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @update:current-page="val => $emit('update:currentPage', val)"
        @update:page-size="val => $emit('update:pageSize', val)"
        @current-change="$emit('query')"
        @size-change="$emit('query')"
      />
    </div>
  </div>
</template>

<script setup>
import { formatDate, formatMoney, formatNumber } from '../../Admin/adminShared.js'

const props = defineProps({
  tableData: { type: Array, default: () => [] },
  total: { type: Number, default: 0 },
  currentPage: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 }
})

defineEmits(['edit', 'delete', 'query', 'update:currentPage', 'update:pageSize'])

const avatarText = name => (name ? String(name).slice(0, 1) : '?')

const getSummaries = ({ columns }) => {
  return columns.map(column => {
    if (column.label === '员工') return '合计'
    if (column.property === 'quantity') {
      return formatNumber(props.tableData.reduce((sum, row) => sum + (Number(row.quantity) || 0), 0))
    }
    if (column.property === 'salary') {
      return formatMoney(props.tableData.reduce((sum, row) => sum + (Number(row.salary) || 0), 0))
    }
    return ''
  })
}
</script>

<style scoped>
.front-table :deep(.cell) {
  white-space: nowrap;
}

.front-table :deep(.el-table__footer .cell) {
  font-weight: 600;
}
</style>
