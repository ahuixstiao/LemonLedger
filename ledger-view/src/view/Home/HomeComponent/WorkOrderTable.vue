<template>
  <div ref="tableSectionRef" class="work-order-table-section-inner">
    <el-table
      :data="tableData"
      class="work-order-table-inner"
      style="width: 100%"
      :max-height="isMobile ? null : tableMaxHeight"
      table-layout="fixed"
      stripe
      fit
      highlight-current-row
      empty-text="暂无数据"
    >
      <el-table-column label="认领" align="center" width="90">
        <template #default="scope">
          <el-button type="warning" @click="$emit('claim', scope.row)">认领</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="factoryName" sortable label="厂名" align="center" show-overflow-tooltip />
      <el-table-column prop="number" sortable label="床号" align="center" show-overflow-tooltip />
      <el-table-column prop="styleNumber" sortable label="款式编号" align="center" show-overflow-tooltip />
      <el-table-column prop="category" sortable label="工作类型" align="center" show-overflow-tooltip />
      <el-table-column prop="quantity" label="数量" align="center" />
      <el-table-column prop="createdDate" sortable label="日期" align="center" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="170">
        <template #default="scope">
          <template v-if="scope.row.flag === 0">
            <el-button text @click="$emit('edit', scope.row)">编辑</el-button>
            <el-button type="danger" text @click="$emit('delete', scope.row.id)">删除</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <div class="work-order-page-inner">
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

defineEmits(['claim', 'edit', 'delete', 'query', 'update:currentPage', 'update:pageSize'])

const tableSectionRef = ref(null)
const tableMaxHeight = ref(360)
const isMobile = ref(false)

const updateMobileState = () => {
  isMobile.value = window.matchMedia('(max-width: 768px)').matches
}

const calcTableMaxHeight = () => {
  if (isMobile.value) return
  const sectionEl = tableSectionRef.value
  if (!sectionEl) return
  const sectionHeight = sectionEl.clientHeight
  const pagerEl = sectionEl.querySelector('.work-order-page-inner')
  const pagerHeight = pagerEl ? pagerEl.offsetHeight : 0
  tableMaxHeight.value = Math.max(180, Math.floor(sectionHeight - pagerHeight))
}

onMounted(() => {
  updateMobileState()
  calcTableMaxHeight()
  window.addEventListener('resize', updateMobileState)
  window.addEventListener('resize', calcTableMaxHeight)
})

onUpdated(() => {
  calcTableMaxHeight()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateMobileState)
  window.removeEventListener('resize', calcTableMaxHeight)
})
</script>

<style scoped>
.work-order-table-section-inner {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.work-order-table-inner {
  flex: 1;
  min-height: 0;
}

:deep(.work-order-table-inner .cell) {
  white-space: nowrap;
}

.work-order-page-inner {
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
  .work-order-table-section-inner {
    overflow: auto;
    -webkit-overflow-scrolling: touch;
  }

  .work-order-page-inner {
    justify-content: center;
  }

  :deep(.work-order-table-inner .el-scrollbar__wrap) {
    overflow: auto;
    -webkit-overflow-scrolling: touch;
  }
}
</style>
