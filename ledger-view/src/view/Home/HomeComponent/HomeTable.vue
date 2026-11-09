<template>
  <div class="front-table-wrap">
    <template v-if="mobile">
      <div class="m-list">
        <article class="m-item" v-for="row in tableData" :key="row.id">
          <div class="m-item__top">
            <span class="entity-cell">
              <span class="entity-avatar">{{ avatarText(row.name) }}</span>
              <span class="entity-name">{{ row.name }}</span>
            </span>
            <span class="money">{{ formatMoney(row.salary) }}</span>
          </div>
          <p class="m-item__sub">
            <span>{{ row.factoryName }}</span>
            <span>床号 {{ row.number }}</span>
            <span>款式 {{ row.styleNumber }}</span>
          </p>
          <div class="m-item__foot">
            <el-tag size="small" effect="plain" type="info">{{ row.category }}</el-tag>
            <span class="num">{{ formatNumber(row.quantity) }} 件</span>
            <span class="date-cell">{{ formatDate(row.createdDate) }}</span>
            <span class="m-item__actions">
              <el-button link type="primary" size="small" @click="$emit('edit', row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="$emit('delete', row)">删除</el-button>
            </span>
          </div>
        </article>

        <p v-if="!tableData.length" class="m-empty">{{ emptyText }}</p>
      </div>

      <div v-if="tableData.length" class="m-summary">
        <span>本页合计</span>
        <span>
          <strong>{{ formatNumber(totalQuantity) }}</strong> 件 ·
          <strong>{{ formatMoney(totalSalary) }}</strong>
        </span>
      </div>
    </template>

    <template v-else>
      <el-table
        :height="'100%'"
        :data="tableData"
        class="front-table"
        table-layout="fixed"
        stripe
        fit
        highlight-current-row
        :empty-text="emptyText"
      >
      <el-table-column prop="name" label="员工" align="center" min-width="150" sortable show-overflow-tooltip>
        <template #default="scope">
          <span class="entity-cell">
            <span class="entity-avatar">{{ avatarText(scope.row.name) }}</span>
            <span class="entity-name">{{ scope.row.name }}</span>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="factoryName" label="厂名" align="center" min-width="150" sortable show-overflow-tooltip />
      <el-table-column prop="number" label="床号" align="center" min-width="90" sortable show-overflow-tooltip />
      <el-table-column prop="styleNumber" label="款式编号" align="center" min-width="120" sortable show-overflow-tooltip />
      <el-table-column prop="category" label="类型" align="center" min-width="90" sortable show-overflow-tooltip>
        <template #default="scope">
          <el-tag size="small" effect="plain" type="info">{{ scope.row.category }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" align="center" min-width="80">
        <template #default="scope">
          <span class="num">{{ formatNumber(scope.row.quantity) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="salary" label="本床工资" align="center" min-width="110">
        <template #default="scope">
          <span class="money">{{ formatMoney(scope.row.salary) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdDate" label="日期" align="center" min-width="110" sortable show-overflow-tooltip>
        <template #default="scope">
          <span class="date-cell">{{ formatDate(scope.row.createdDate) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="120" fixed="right">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="$emit('edit', scope.row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="$emit('delete', scope.row)">删除</el-button>
        </template>
      </el-table-column>
      </el-table>

      <div v-if="tableData.length" class="front-summary">
        <span>本页合计</span>
        <span>
          <strong>{{ formatNumber(totalQuantity) }}</strong> 件 ·
          <strong>{{ formatMoney(totalSalary) }}</strong>
        </span>
      </div>
    </template>

    <div class="pager-row front-pager">
      <el-pagination
        background
        :total="total"
        :current-page="currentPage"
        :page-size="pageSize"
        :page-sizes="[5, 10, 20, 50, 100]"
        :layout="mobile ? 'prev, pager, next' : 'total, sizes, prev, pager, next'"
        @update:current-page="val => $emit('update:currentPage', val)"
        @update:page-size="val => $emit('update:pageSize', val)"
        @current-change="$emit('query')"
        @size-change="$emit('query')"
      />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { formatDate, formatMoney, formatNumber } from '../../Admin/adminShared.js'

const props = defineProps({
  tableData: { type: Array, default: () => [] },
  total: { type: Number, default: 0 },
  currentPage: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 },
  mobile: { type: Boolean, default: false },
  emptyText: { type: String, default: "还没有记录" }
})

defineEmits(['edit', 'delete', 'query', 'update:currentPage', 'update:pageSize'])

const avatarText = name => (name ? String(name).slice(0, 1) : '?')

const totalQuantity = computed(() =>
  props.tableData.reduce((sum, row) => sum + (Number(row.quantity) || 0), 0)
)

const totalSalary = computed(() =>
  props.tableData.reduce((sum, row) => sum + (Number(row.salary) || 0), 0)
)
</script>

<style scoped>
.front-table :deep(.cell) {
  white-space: nowrap;
}

.m-empty {
  padding: 30px 0;
  text-align: center;
  font-size: 13px;
  color: var(--admin-text-muted);
}
</style>
