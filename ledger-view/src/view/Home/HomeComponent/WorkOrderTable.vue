<template>
  <div class="front-table-wrap">
    <template v-if="mobile">
      <div class="m-list">
        <article class="m-item" v-for="row in tableData" :key="row.id">
          <div class="m-item__top">
            <span class="entity-name">{{ row.factoryName }}</span>
            <span v-if="Number(row.remainQuantity) <= 0" class="status-pill is-active">已认满</span>
            <span v-else class="status-pill is-remaining">剩 {{ formatNumber(row.remainQuantity) }}</span>
          </div>
          <p class="m-item__sub">
            <span>床号 {{ row.number }}</span>
            <span>款式 {{ row.styleNumber }}</span>
            <el-tag size="small" effect="plain" type="info">{{ row.category }}</el-tag>
          </p>
          <div class="m-item__foot">
            <span class="num">{{ formatNumber(row.quantity) }} 件</span>
            <span class="num">压花 {{ formatNumber(row.claimedEmboss) }}</span>
            <span class="num">刮胶 {{ formatNumber(row.claimedGlue) }}</span>
            <span class="date-cell">{{ formatDate(row.createdDate) }}</span>
          </div>
          <div class="m-item__actions">
            <template v-if="row.flag === 0">
              <el-button link type="primary" size="small" @click="$emit('edit', row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="$emit('delete', row)">删除</el-button>
            </template>
            <span v-else class="readonly-text">不可操作</span>
          </div>
          <el-button
            v-if="Number(row.remainQuantity) > 0 && row.flag === 0"
            type="primary"
            plain
            class="m-item__claim"
            @click="$emit('claim', row)"
          >
            认领
          </el-button>
        </article>

        <p v-if="!tableData.length" class="m-empty">{{ emptyText }}</p>
      </div>

      <div v-if="tableData.length" class="m-summary">
        <span>本页合计</span>
        <span>
          <strong>{{ formatNumber(totalQuantity) }}</strong> 件 ·
          压花 <strong>{{ formatNumber(totalEmboss) }}</strong> ·
          刮胶 <strong>{{ formatNumber(totalGlue) }}</strong> ·
          剩 <strong>{{ formatNumber(totalRemain) }}</strong>
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
      <el-table-column label="认领" align="center" width="76">
        <template #default="scope">
          <el-button
            v-if="Number(scope.row.remainQuantity) > 0 && scope.row.flag === 0"
            type="primary"
            plain
            size="small"
            @click="$emit('claim', scope.row)"
          >
            认领
          </el-button>
          <span v-else class="readonly-text">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="factoryName" label="厂名" align="center" min-width="140" sortable show-overflow-tooltip />
      <el-table-column prop="number" label="床号" align="center" min-width="88" sortable show-overflow-tooltip />
      <el-table-column prop="styleNumber" label="款式编号" align="center" min-width="112" sortable show-overflow-tooltip />
      <el-table-column prop="category" label="工作类型" align="center" min-width="92" sortable show-overflow-tooltip>
        <template #default="scope">
          <el-tag size="small" effect="plain" type="info">{{ scope.row.category }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" align="center" min-width="76">
        <template #default="scope">
          <span class="num">{{ formatNumber(scope.row.quantity) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="claimedEmboss" label="已认领·压花" align="center" min-width="100">
        <template #default="scope">
          <span class="num">{{ formatNumber(scope.row.claimedEmboss) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="claimedGlue" label="已认领·刮胶" align="center" min-width="100">
        <template #default="scope">
          <span class="num">{{ formatNumber(scope.row.claimedGlue) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="剩余" align="center" min-width="88">
        <template #default="scope">
          <span v-if="Number(scope.row.remainQuantity) <= 0" class="status-pill is-active">已认满</span>
          <span v-else class="status-pill is-remaining">{{ formatNumber(scope.row.remainQuantity) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdDate" label="日期" align="center" min-width="104" sortable show-overflow-tooltip>
        <template #default="scope">
          <span class="date-cell">{{ formatDate(scope.row.createdDate) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="124" fixed="right">
        <template #default="scope">
          <template v-if="scope.row.flag === 0">
            <el-button link type="primary" size="small" @click="$emit('edit', scope.row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="$emit('delete', scope.row)">删除</el-button>
          </template>
          <span v-else class="readonly-text">不可操作</span>
        </template>
      </el-table-column>
      </el-table>

      <div v-if="tableData.length" class="front-summary">
        <span>本页合计</span>
        <span>
          <strong>{{ formatNumber(totalQuantity) }}</strong> 件 ·
          压花 <strong>{{ formatNumber(totalEmboss) }}</strong> ·
          刮胶 <strong>{{ formatNumber(totalGlue) }}</strong> ·
          剩 <strong>{{ formatNumber(totalRemain) }}</strong>
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

defineEmits(['claim', 'edit', 'delete', 'query', 'update:currentPage', 'update:pageSize'])

const sumBy = prop =>
  props.tableData.reduce((sum, row) => sum + (Number(row[prop]) || 0), 0)

const totalQuantity = computed(() => sumBy('quantity'))
const totalEmboss = computed(() => sumBy('claimedEmboss'))
const totalGlue = computed(() => sumBy('claimedGlue'))
const totalRemain = computed(() => sumBy('remainQuantity'))
</script>

<style scoped>
.front-table :deep(.cell) {
  white-space: nowrap;
}

.status-pill.is-remaining {
  background: #fff4e6;
  color: #d97706;
}

.m-item__top .status-pill {
  flex-shrink: 0;
}

.m-empty {
  padding: 30px 0;
  text-align: center;
  font-size: 13px;
  color: var(--admin-text-muted);
}
</style>
