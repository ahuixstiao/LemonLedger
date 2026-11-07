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
      empty-text="还没有待认领的工单"
      show-summary
      :summary-method="getSummaries"
    >
      <el-table-column label="认领" align="center" width="96">
        <template #default="scope">
          <el-button type="primary" plain size="small" @click="$emit('claim', scope.row)">认领</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="factoryName" label="厂名" align="center" min-width="190" sortable show-overflow-tooltip />
      <el-table-column prop="number" label="床号" align="center" min-width="110" sortable show-overflow-tooltip />
      <el-table-column prop="styleNumber" label="款式编号" align="center" min-width="160" sortable show-overflow-tooltip />
      <el-table-column prop="category" label="工作类型" align="center" min-width="120" sortable show-overflow-tooltip>
        <template #default="scope">
          <el-tag size="small" effect="plain" type="info">{{ scope.row.category }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" align="center" min-width="100">
        <template #default="scope">
          <span class="num">{{ formatNumber(scope.row.quantity) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="claimedQuantity" label="已认领" align="center" min-width="110">
        <template #default="scope">
          <span class="num">{{ formatNumber(scope.row.claimedQuantity) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="剩余" align="center" min-width="110">
        <template #default="scope">
          <span v-if="Number(scope.row.remainQuantity) <= 0" class="status-pill is-active">已认满</span>
          <span v-else class="status-pill is-remaining">{{ formatNumber(scope.row.remainQuantity) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdDate" label="日期" align="center" min-width="140" sortable show-overflow-tooltip>
        <template #default="scope">
          <span class="date-cell">{{ formatDate(scope.row.createdDate) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="168" fixed="right">
        <template #default="scope">
          <template v-if="scope.row.flag === 0">
            <el-button link type="primary" size="small" @click="$emit('edit', scope.row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="$emit('delete', scope.row)">删除</el-button>
          </template>
          <span v-else class="readonly-text">不可操作</span>
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
import { formatDate, formatNumber } from '../../Admin/adminShared.js'

const props = defineProps({
  tableData: { type: Array, default: () => [] },
  total: { type: Number, default: 0 },
  currentPage: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 }
})

defineEmits(['claim', 'edit', 'delete', 'query', 'update:currentPage', 'update:pageSize'])

const sumBy = prop =>
  props.tableData.reduce((sum, row) => sum + (Number(row[prop]) || 0), 0)

const getSummaries = ({ columns }) => {
  return columns.map(column => {
    if (column.label === '认领') return '总计'
    if (['quantity', 'claimedQuantity'].includes(column.property)) {
      return formatNumber(sumBy(column.property))
    }
    if (column.label === '剩余') {
      return formatNumber(sumBy('remainQuantity'))
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

.status-pill.is-remaining {
  background: #fff4e6;
  color: #d97706;
}
</style>
