<template>
  <div class="admin-page">
    <AdminPageHeader
      title="成衣厂计件单价"
      desc="一个「成衣厂 + 款式编号 + 工作类型」对应一个厂价，账单金额按这里的单价计算。"
      :count="data.total"
      count-label="条单价"
    >
      <template #actions>
        <el-button type="primary" @click="openCreatePriceDialog">添加工厂单价</el-button>
      </template>
    </AdminPageHeader>

    <div class="filter-card">
      <div class="filter-bar">
        <el-select
          v-model="data.factoryId"
          filterable
          clearable
          placeholder="全部成衣厂"
          class="filter-wide"
          @change="onFilterChange"
        >
          <el-option v-for="item in data.factoryList" :key="item.id" :label="item.factoryName" :value="item.id" />
        </el-select>
        <el-input
          v-model="data.styleNumber"
          clearable
          placeholder="搜索款式编号"
          @input="onSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="data.categoryId" clearable placeholder="工作类型" @change="onFilterChange">
          <el-option v-for="item in data.categoryList" :key="item.id" :label="item.category" :value="item.id" />
        </el-select>
        <el-select v-model="data.flag" placeholder="状态" @change="onFilterChange">
          <el-option label="正常" :value="0" />
          <el-option label="删除" :value="1" />
        </el-select>
        <div class="filter-actions">
          <el-button @click="resetPriceFilter">重置</el-button>
        </div>
      </div>
    </div>

    <div class="table-card">
      <el-table height="100%" :data="data.tableData" stripe highlight-current-row empty-text="没有找到成衣厂计件单价">
        <el-table-column prop="factoryName" sortable label="厂名" min-width="190" align="center">
          <template #default="scope">
            <div class="entity-cell">
              <span class="entity-avatar">{{ (scope.row.factoryName || '?').slice(0, 1) }}</span>
              <span class="entity-name">{{ scope.row.factoryName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="styleNumber" sortable label="款式编号" min-width="160" align="center" />
        <el-table-column prop="category" sortable label="工作类型" min-width="120" align="center">
          <template #default="scope">
            <el-tag type="info" effect="plain">{{ scope.row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" sortable label="计件单价" min-width="120" align="center">
          <template #default="scope">
            <span class="money">{{ formatMoney(scope.row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="样板" min-width="90" align="center">
          <template #default="scope">
            <el-image
              v-if="scope.row.imagePath"
              :src="resolveImageUrl(scope.row.imagePath)"
              :preview-src-list="[resolveImageUrl(scope.row.imagePath)]"
              fit="cover"
              class="sample-image"
              preview-teleported
            />
            <span v-else class="muted">暂无</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdDate" sortable label="创建日期" min-width="140" align="center">
          <template #default="scope">
            <span class="date-cell">{{ formatDate(scope.row.createdDate) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="flag" sortable label="状态" min-width="110" align="center">
          <template #default="scope">
            <span
              class="status-pill"
              :class="scope.row.flag === 0 ? 'is-active' : 'is-removed'"
            >{{ scope.row.flag === 0 ? '正常' : '删除' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="150" align="center">
          <template #default="scope">
            <span v-if="isReadOnlyRow(scope.row)" class="readonly-text">已删除，不可操作</span>
            <template v-else>
              <el-button text type="primary" @click="openEditPriceDialog(scope.row)">编辑</el-button>
              <el-button text type="danger" @click="removeFactoryPieceRate(scope.row)">删除</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </div>


    <div class="pager-row">
      <el-pagination
        background
        :total="data.total"
        v-model:current-page="data.currentPage"
        v-model:page-size="data.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchFactoryPieceRateList"
        @size-change="onSizeChange"
      />
    </div>

    <el-dialog
      v-model="data.editPriceDialogVisible"
      :title="data.editPriceDialogMode === 0 ? '添加工厂计件单价' : '修改工厂计件单价'"
      width="520px"
    >
      <el-form
        ref="priceInfoFormRef"
        :model="factoryPieceRateInfoRef"
        :rules="addPriceInfoRules"
        label-position="top"
      >
        <el-form-item label="成衣厂" prop="factoryId">
          <el-select filterable v-model="factoryPieceRateInfoRef.factoryId" placeholder="选择成衣厂" @change="onFactoryChange">
            <el-option v-for="item in data.factoryList" :key="item.id" :label="item.factoryName" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="款式编号" prop="styleId">
          <el-select
            ref="styleSelectRef"
            v-model="factoryPieceRateInfoRef.styleId"
            filterable
            placeholder="先选成衣厂，再选款式"
            :disabled="!factoryPieceRateInfoRef.factoryId"
          >
            <el-option
              v-for="item in data.styleList"
              :key="item.id"
              :label="item.styleNumber"
              :value="item.id"
            />
          </el-select>
          <div v-if="!data.styleList.length && factoryPieceRateInfoRef.factoryId" class="sync-hint">
            这个成衣厂还没有款式，先去「成衣厂款式」里添加。
          </div>
        </el-form-item>

        <el-form-item label="工作类型" prop="categoryId">
          <el-select v-model="factoryPieceRateInfoRef.categoryId" placeholder="选择工作类型">
            <el-option v-for="item in data.categoryList" :key="item.id" :label="item.category" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="计件单价（元 / 件）" prop="price">
          <el-input v-model="factoryPieceRateInfoRef.price" placeholder="输入计件单价">
            <template #prefix>¥</template>
          </el-input>
        </el-form-item>

        <el-form-item v-if="data.editPriceDialogMode === 1" label="受影响的账单日期范围">
          <div class="sync-range">
            <el-date-picker
              v-model="syncDateRange.startDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="开始日期"
            />
            <span class="sync-arrow">→</span>
            <el-date-picker
              v-model="syncDateRange.endDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="结束日期"
            />
          </div>
          <div class="sync-hint">
            只重算这个区间里用到这条单价的账单金额（数量 × 新单价），已导出的结算快照不会变。
          </div>
        </el-form-item>

        <el-form-item label="样板图片（在「成衣厂款式」里维护）">
          <div class="sample-upload-row">
            <el-image
              v-if="dialogStyleImage"
              :src="resolveImageUrl(dialogStyleImage)"
              :preview-src-list="[resolveImageUrl(dialogStyleImage)]"
              fit="cover"
              class="sample-image"
              preview-teleported
            />
            <span v-else class="muted">这个款式还没有图片</span>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-hotkey-hint">Ctrl + Enter 提交 · Esc 关闭</span>
        <el-button @click="closePriceDialog">取消</el-button>
        <el-button type="primary" @click="validatePriceForm(priceInfoFormRef)">
          {{ data.editPriceDialogMode === 0 ? '新增' : '保存' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import AdminPageHeader from '../AdminPageHeader.vue'
import { confirmDelete, formatDate, formatMoney } from '../adminShared.js'
import { useDialogShortcuts } from '../../../base/dialogShortcuts.js'
import { queryFactoryList } from '../../../network/index.js'
import {
  deleteFactoryPieceRateInfo,
  queryFactoryPieceRateList,
  queryJobCategoryList,
  saveFactoryPieceRateInfo,
  updateFactoryPieceRateInfo
} from '../../../network/admin/index.js'
import { queryStyleListByFactoryId } from '../../../network/admin/index.js'
import {
  loadFactoryOptions,
  loadFactoryCategoryOptions,
  openCreateDialog,
  openEditDialog,
  resetReactiveForm,
  validateDialogForm
} from './factoryCrudShared.js'

// 改单价时用户要指定受影响的账单区间，默认给「今年 1 月 1 日 → 今天」，覆盖最常见的补账场景
const syncDateRange = reactive({ startDate: '', endDate: '' })
const originalPrice = ref('')

const data = reactive({
  tableData: [],
  factoryList: [],
  categoryList: [],
  factoryId: '',
  styleNumber: '',
  categoryId: '',
  flag: 0,
  currentPage: 1,
  pageSize: 10,
  total: 0,
  styleList: [],
  editPriceDialogVisible: false,
  editPriceDialogMode: 0,
  imageVersion: 0
})

onMounted(() => {
  loadFactoryList()
  loadCategoryList()
  fetchFactoryPieceRateList()
})

const loadFactoryList = async () => {
  await loadFactoryOptions(data, queryFactoryList)
}

/**
 * 按成衣厂加载款式下拉选项。
 *
 * @param {number|string} factoryId 成衣厂ID
 */
const loadStyleOptions = async factoryId => {
  if (!factoryId) {
    data.styleList = []
    return
  }
  const { data: res } = await queryStyleListByFactoryId(factoryId, 0)
  data.styleList = res.status === 200 ? (res.data || []) : []
}

/**
 * 弹窗里当前选中款式的图片地址（只读展示）。
 */
const dialogStyleImage = computed(() => {
  const style = data.styleList.find(item => String(item.id) === String(factoryPieceRateInfoRef.styleId))
  return style?.imagePath || ''
})

/**
 * 成衣厂切换后重新加载款式，并清掉已经选中的款式。
 */
const onFactoryChange = async () => {
  factoryPieceRateInfoRef.styleId = ''
  await loadStyleOptions(factoryPieceRateInfoRef.factoryId)
}

const styleSelectRef = ref()

const loadCategoryList = async () => {
  await loadFactoryCategoryOptions(data, queryJobCategoryList)
}

const fetchFactoryPieceRateList = async () => {
  const { data: res } = await queryFactoryPieceRateList(
    data.factoryId,
    data.styleNumber,
    data.categoryId,
    data.currentPage,
    data.pageSize,
    data.flag
  )
  if (res.status === 200) {
    data.tableData = res.data.records
    data.total = res.data.total
  } else {
    data.tableData = []
    data.total = 0
  }
}

const onSearch = () => {
  data.currentPage = 1
  fetchFactoryPieceRateList()
}

const onFilterChange = () => {
  data.currentPage = 1
  fetchFactoryPieceRateList()
}

const onSizeChange = () => {
  data.currentPage = 1
  fetchFactoryPieceRateList()
}

const resetPriceFilter = () => {
  data.factoryId = ''
  data.styleNumber = ''
  data.categoryId = ''
  data.flag = 0
  data.currentPage = 1
  fetchFactoryPieceRateList()
}

/**
 * 删除状态的数据仅允许展示，不提供编辑/删除入口。
 */
const isReadOnlyRow = row => {
  return Number(row?.flag) === 1 || Number(data.flag) === 1
}

const createFactoryPieceRate = async () => {
  const { data: res } = await saveFactoryPieceRateInfo(factoryPieceRateInfoRef)
  if (res.status === 200) {
    ElMessage.success(`${res.message}，已保留成衣厂/工作类型/单价，换个款式编号就能继续加`)
    // 连续录入同一批单价是常态，这里不关弹窗，也不把整张表单清空：
    // 只清「款式编号」和 id、图片，成衣厂、工作类型、单价留着，
    // 因为同一批单价往往就是同厂同工种同价、只换款式。
    data.editPriceDialogMode = 0
    factoryPieceRateInfoRef.id = ''
    factoryPieceRateInfoRef.styleId = ''
    priceInfoFormRef.value?.clearValidate?.()
    // 焦点回款式编号，接着录下一个款
    nextTick(() => styleSelectRef.value?.focus?.())
    await fetchFactoryPieceRateList()
  } else {
    ElMessage.error(res.message)
  }
}

const updateFactoryPieceRate = async () => {
  const nextPrice = Number(factoryPieceRateInfoRef.price)
  const previousPrice = originalPrice.value === '' ? Number.NaN : Number(originalPrice.value)
  const priceChanged = Number.isFinite(nextPrice) && nextPrice !== previousPrice

  // 只有单价变了才要求选区间；只改款式/工种不需要重算账单
  if (priceChanged && (!syncDateRange.startDate || !syncDateRange.endDate)) {
    ElMessage.error('单价改了，请先选择受影响的账单日期范围')
    return
  }

  const { data: res } = await updateFactoryPieceRateInfo(
    factoryPieceRateInfoRef,
    priceChanged ? syncDateRange.startDate : '',
    priceChanged ? syncDateRange.endDate : ''
  )
  if (res.status === 200) {
    ElMessage.success(res.message)
    data.editPriceDialogVisible = false
    await fetchFactoryPieceRateList()
  } else {
    ElMessage.error(res.message)
  }
}

const removeFactoryPieceRate = async row => {
  const confirmed = await confirmDelete(
    `确定删除「${row.factoryName} · ${row.styleNumber} · ${row.category}」这条单价吗？`
  )
  if (!confirmed) return

  const { data: res } = await deleteFactoryPieceRateInfo(row.id)
  if (res.status === 200) {
    ElMessage.success(res.message)
    await fetchFactoryPieceRateList()
  } else {
    ElMessage.error(res.message)
  }
}

const validatePriceForm = async formEl => {
  await validateDialogForm(
    formEl,
    data.editPriceDialogMode,
    createFactoryPieceRate,
    updateFactoryPieceRate,
    () => ElMessage.error('请检查是否填写正确')
  )
}

const priceInfoFormRef = ref()

/**
 * 将相对图片路径转换为可访问的完整URL。
 *
 * @param {string} imagePath 图片路径
 * @return {string} 图片完整地址
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
    const relativePath = normalizedPath.slice(imagePrefix.length)
    return `${baseUrl}/admin/factoryStyle/imageView/${relativePath}?v=${data.imageVersion}`
  }

  return `${baseUrl}${normalizedPath}?v=${data.imageVersion}`
}

/**
 * 在弹窗中删除当前单价单图片。
 */
// 成衣厂计件单价实体初始化
const factoryPieceRateInfoInit = {
  id: '',
  factoryId: '',
  styleId: '',
  categoryId: '',
  price: ''
}

const factoryPieceRateInfoRef = reactive({ ...factoryPieceRateInfoInit })

const openCreatePriceDialog = async () => {
  openCreateDialog(
    factoryPieceRateInfoRef,
    factoryPieceRateInfoInit,
    data,
    'editPriceDialogMode',
    'editPriceDialogVisible'
  )
  await loadStyleOptions(factoryPieceRateInfoRef.factoryId)
  nextTick(() => styleSelectRef.value?.focus?.())
}

const openEditPriceDialog = async priceInfo => {
  openEditDialog(
    factoryPieceRateInfoRef,
    priceInfo,
    data,
    'editPriceDialogMode',
    'editPriceDialogVisible'
  )
  await loadStyleOptions(factoryPieceRateInfoRef.factoryId)
  originalPrice.value = priceInfo?.price ?? ''
  const today = new Date()
  syncDateRange.startDate = `${today.getFullYear()}-01-01`
  syncDateRange.endDate = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`
}

const resetPriceForm = () => {
  resetReactiveForm(factoryPieceRateInfoRef, factoryPieceRateInfoInit, priceInfoFormRef)
}

const closePriceDialog = () => {
  resetPriceForm()
  syncDateRange.startDate = ''
  syncDateRange.endDate = ''
  originalPrice.value = ''
  data.editPriceDialogVisible = false
}

const addPriceInfoRules = reactive({
  factoryId: [{ required: true, message: '请选择成衣厂', trigger: 'blur' }],
  styleId: [{ required: true, message: '请选择款式编号', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择工作类型', trigger: 'blur' }],
  price: [{ required: true, message: '请输入计件单价', trigger: 'blur' }]
})
useDialogShortcuts({
  isOpen: () => data.editPriceDialogVisible,
  onConfirm: () => validatePriceForm(priceInfoFormRef.value),
  onCancel: closePriceDialog
})
</script>

<style scoped>
.hidden-upload-input {
  display: none;
}

.sample-image {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  border: 1px solid #e9eef7;
  cursor: zoom-in;
}

.sync-range {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.sync-range :deep(.el-date-editor) {
  flex: 1 1 0;
  width: auto;
}

.sync-arrow {
  color: var(--admin-text-muted, #98a4bb);
}

.sync-hint {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.6;
  color: var(--admin-text-muted, #98a4bb);
}
.sample-upload-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
</style>
