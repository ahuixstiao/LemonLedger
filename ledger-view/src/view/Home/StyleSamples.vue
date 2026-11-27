<template>
  <FrontShell>
    <div class="admin-page">
      <div class="admin-page-head">
        <div>
          <h2 class="admin-page-head__title">压花图片</h2>
          <p class="admin-page-head__desc">
            按成衣厂和款式编号查样板图片；图片统一在后台「成衣厂款式」里维护，这里只看不改。
          </p>
        </div>
        <div class="admin-page-head__aside">
          <span class="admin-count-chip"><strong>{{ data.total }}</strong> 个款式</span>
          <el-button :loading="data.loading" @click="fetchSampleList">刷新</el-button>
        </div>
      </div>

      <div class="filter-card">
        <div class="filter-bar">
          <el-select
            v-model="data.factoryId"
            clearable
            placeholder="全部成衣厂"
            class="filter-wide"
            @change="onFilterChange"
          >
            <el-option
              v-for="item in data.factoryList"
              :key="item.id"
              :label="item.factoryName"
              :value="item.id"
            />
          </el-select>
          <el-input
            v-model="data.styleNumber"
            clearable
            placeholder="搜索款式编号"
            @keyup.enter="onFilterChange"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <div class="filter-actions">
            <el-button type="primary" @click="onFilterChange">查询</el-button>
            <el-button @click="onReset">重置</el-button>
          </div>
        </div>
      </div>

      <div class="table-card">
        <div v-if="data.tableData.length" class="sample-grid">
          <div v-for="item in data.tableData" :key="item.id" class="sample-card">
            <el-image
              :src="resolveImageUrl(item.imagePath)"
              :preview-src-list="[resolveImageUrl(item.imagePath)]"
              fit="cover"
              class="sample-image"
              preview-teleported
            >
              <template #error>
                <div class="sample-image-empty">图片丢失</div>
              </template>
            </el-image>
            <div class="sample-style-number">{{ item.styleNumber }}</div>
            <div class="sample-factory">{{ item.factoryName || '—' }}</div>
          </div>
        </div>
        <div v-else class="sample-empty">
          {{ data.loading ? '正在加载…' : '没有符合条件的样板图片，换个成衣厂或款式编号试试' }}
        </div>

        <div v-if="data.total > 0" class="pager-row front-pager">
          <el-pagination
            background
            :total="data.total"
            :current-page="data.currentPage"
            :page-size="data.pageSize"
            :page-sizes="[12, 24, 48, 96]"
            :layout="data.isNarrow ? 'prev, pager, next' : 'total, sizes, prev, pager, next'"
            @update:current-page="val => (data.currentPage = val)"
            @update:page-size="val => (data.pageSize = val)"
            @current-change="fetchSampleList"
            @size-change="fetchSampleList"
          />
        </div>
      </div>
    </div>
  </FrontShell>
</template>

<script setup>
import { reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import FrontShell from './HomeComponent/FrontShell.vue'
import { queryFactoryList, queryStyleSampleList } from '../../network/factory.js'

const data = reactive({
  tableData: [],
  total: 0,
  currentPage: 1,
  pageSize: 12,
  factoryId: '',
  styleNumber: '',
  factoryList: [],
  loading: false,
  isNarrow: window.matchMedia('(max-width: 960px)').matches
})

/**
 * 查询样板图片列表（数据来自后台的成衣厂款式表，只返回有图片的款式）。
 */
const fetchSampleList = async () => {
  data.loading = true
  try {
    const { data: res } = await queryStyleSampleList(
      data.factoryId,
      data.styleNumber,
      data.currentPage,
      data.pageSize
    )
    if (res.status === 200) {
      data.tableData = res.data.records || []
      data.total = res.data.total || 0
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('查询样板图片失败，请稍后再试')
  } finally {
    data.loading = false
  }
}

/**
 * 查询成衣厂下拉数据。
 */
const fetchFactoryList = async () => {
  try {
    const { data: res } = await queryFactoryList()
    if (res.status === 200) {
      data.factoryList = res.data || []
    }
  } catch (error) {
    // 下拉为空不影响列表查看，静默处理
  }
}

/**
 * 条件变化或点查询时回到第一页再查。
 */
const onFilterChange = () => {
  data.currentPage = 1
  fetchSampleList()
}

/**
 * 重置筛选条件。
 */
const onReset = () => {
  data.factoryId = ''
  data.styleNumber = ''
  data.currentPage = 1
  fetchSampleList()
}

/**
 * 把库里存的相对图片路径转换成可访问的完整地址。
 */
const resolveImageUrl = imagePath => {
  if (!imagePath) {
    return ''
  }
  const normalizedPath = String(imagePath)
  if (normalizedPath.startsWith('http')) {
    return normalizedPath
  }
  const baseUrl = (import.meta.env.VITE_BASE_URL || '').replace(/\/$/, '')
  const imagePrefix = '/lemonLedgerImages/'
  if (normalizedPath.startsWith(imagePrefix)) {
    return `${baseUrl}/admin/factoryStyle/imageView/${normalizedPath.slice(imagePrefix.length)}`
  }
  return `${baseUrl}${normalizedPath.startsWith('/') ? '' : '/'}${normalizedPath}`
}

/**
 * 视口变窄时切换分页布局。
 */
const updateNarrowState = () => {
  data.isNarrow = window.matchMedia('(max-width: 960px)').matches
}

onMounted(() => {
  updateNarrowState()
  window.addEventListener('resize', updateNarrowState)
  fetchFactoryList()
  fetchSampleList()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateNarrowState)
})
</script>

<style scoped>
.sample-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(196px, 1fr));
  gap: 12px;
}

.sample-card {
  border: 1px solid var(--admin-card-border);
  border-radius: 12px;
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  background: #ffffff;
}

.sample-image {
  width: 100%;
  height: 172px;
  border-radius: 8px;
  background: #f2f5fa;
  cursor: zoom-in;
}

.sample-image-empty {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  color: var(--admin-text-muted);
  font-size: 12px;
}

.sample-style-number {
  font-size: 12.5px;
  font-weight: 600;
  color: #1f2a44;
  text-align: center;
  word-break: break-all;
}

.sample-factory {
  font-size: 12px;
  color: var(--admin-text-secondary);
  text-align: center;
  word-break: break-all;
}

@media (max-width: 640px) {
  .sample-grid {
    grid-template-columns: repeat(auto-fill, minmax(148px, 1fr));
    gap: 10px;
  }

  .sample-image {
    height: 126px;
  }
}

.sample-empty {
  padding: 48px 0;
  text-align: center;
  color: var(--admin-text-muted);
  font-size: 13px;
}
</style>
