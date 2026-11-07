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

    <input
      ref="priceImageUploadInputRef"
      type="file"
      accept="image/*"
      class="hidden-upload-input"
      @change="handlePriceImageUpload"
    />

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
          <el-select filterable v-model="factoryPieceRateInfoRef.factoryId" placeholder="选择成衣厂">
            <el-option v-for="item in data.factoryList" :key="item.id" :label="item.factoryName" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="款式编号" prop="styleNumber">
          <el-input v-model="factoryPieceRateInfoRef.styleNumber" placeholder="输入款式编号" />
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

        <el-form-item v-if="data.editPriceDialogMode === 1" label="样板图片">
          <div class="sample-upload-row">
            <el-button :disabled="!factoryPieceRateInfoRef.id" @click="triggerPriceImageUpload(factoryPieceRateInfoRef.id)">
              上传图片
            </el-button>
            <el-button
              type="danger"
              plain
              :disabled="!factoryPieceRateInfoRef.id || !factoryPieceRateInfoRef.imagePath"
              @click="removePriceImageByDialog"
            >
              删除图片
            </el-button>
            <el-image
              v-if="factoryPieceRateInfoRef.imagePath"
              :src="resolveImageUrl(factoryPieceRateInfoRef.imagePath)"
              :preview-src-list="[resolveImageUrl(factoryPieceRateInfoRef.imagePath)]"
              fit="cover"
              class="sample-image"
              preview-teleported
            />
            <span v-else class="muted">暂无图片</span>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closePriceDialog">取消</el-button>
        <el-button type="primary" @click="validatePriceForm(priceInfoFormRef)">
          {{ data.editPriceDialogMode === 0 ? '新增' : '保存' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import AdminPageHeader from '../AdminPageHeader.vue'
import { confirmDelete, formatDate, formatMoney } from '../adminShared.js'
import { queryFactoryList } from '../../../network/index.js'
import {
  deleteFactoryPieceRateInfo,
  deleteFactoryPieceRateImage,
  queryFactoryPieceRateList,
  queryJobCategoryList,
  saveFactoryPieceRateInfo,
  updateFactoryPieceRateInfo,
  uploadFactoryPieceRateImage
} from '../../../network/admin/index.js'
import {
  loadFactoryOptions,
  loadFactoryCategoryOptions,
  openCreateDialog,
  openEditDialog,
  resetReactiveForm,
  validateDialogForm
} from './factoryCrudShared.js'

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
  uploadingPieceRateId: '',
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
    factoryPieceRateInfoRef.styleNumber = ''
    factoryPieceRateInfoRef.imagePath = ''
    priceInfoFormRef.value?.clearValidate?.()
    if (priceImageUploadInputRef.value) {
      priceImageUploadInputRef.value.value = ''
    }
    await fetchFactoryPieceRateList()
  } else {
    ElMessage.error(res.message)
  }
}

const updateFactoryPieceRate = async () => {
  const { data: res } = await updateFactoryPieceRateInfo(factoryPieceRateInfoRef)
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
const priceImageUploadInputRef = ref(null)

/**
 * 触发隐藏文件选择框，并记录当前待上传的计件单价单ID。
 *
 * @param {number|string} pieceRateId 计件单价单ID
 */
const triggerPriceImageUpload = pieceRateId => {
  data.uploadingPieceRateId = pieceRateId
  if (priceImageUploadInputRef.value) {
    priceImageUploadInputRef.value.value = ''
    priceImageUploadInputRef.value.click()
  }
}

/**
 * 校验并上传计件单价单图片。
 *
 * @param {Event} event 文件选择事件
 */
const handlePriceImageUpload = async event => {
  const target = event?.target
  const file = target?.files?.[0]

  if (!file) {
    return
  }

  if (!data.uploadingPieceRateId) {
    ElMessage.error('未识别到对应计件单价单，请重试')
    target.value = ''
    return
  }

  if (!file.type.startsWith('image/')) {
    ElMessage.error('仅支持上传图片文件')
    target.value = ''
    return
  }

  const maxFileSize = 100 * 1024 * 1024
  if (file.size > maxFileSize) {
    ElMessage.error('图片大小不能超过 100MB')
    target.value = ''
    return
  }

  try {
    const formData = new FormData()
    formData.append('file', file)

    const { data: res } = await uploadFactoryPieceRateImage(data.uploadingPieceRateId, formData)
    if (res.status === 200) {
      ElMessage.success(res.message || '上传成功')
      if (String(factoryPieceRateInfoRef.id) === String(data.uploadingPieceRateId)) {
        factoryPieceRateInfoRef.imagePath = res.data || factoryPieceRateInfoRef.imagePath
      }
      data.imageVersion += 1
      await fetchFactoryPieceRateList()
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (error) {
    ElMessage.error('上传失败，请稍后重试')
  } finally {
    data.uploadingPieceRateId = ''
    target.value = ''
  }
}

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
    return `${baseUrl}/admin/factoryPieceRate/imageView/${relativePath}?v=${data.imageVersion}`
  }

  return `${baseUrl}${normalizedPath}?v=${data.imageVersion}`
}

/**
 * 在弹窗中删除当前单价单图片。
 */
const removePriceImageByDialog = async () => {
  if (!factoryPieceRateInfoRef.id) {
    ElMessage.error('请先保存计件单价单，再删除图片')
    return
  }

  const confirmed = await confirmDelete('确定删除这张样板图片吗？')
  if (!confirmed) return

  const { data: res } = await deleteFactoryPieceRateImage(factoryPieceRateInfoRef.id)
  if (res.status === 200) {
    ElMessage.success(res.message || '删除图片成功')
    factoryPieceRateInfoRef.imagePath = res.data
    data.imageVersion += 1
    await fetchFactoryPieceRateList()
  } else {
    ElMessage.error(res.message || '删除图片失败')
  }
}

// 成衣厂计件单价实体初始化
const factoryPieceRateInfoInit = {
  id: '',
  factoryId: '',
  styleNumber: '',
  categoryId: '',
  price: '',
  imagePath: ''
}

const factoryPieceRateInfoRef = reactive({ ...factoryPieceRateInfoInit })

const openCreatePriceDialog = () => {
  openCreateDialog(
    factoryPieceRateInfoRef,
    factoryPieceRateInfoInit,
    data,
    'editPriceDialogMode',
    'editPriceDialogVisible'
  )
}

const openEditPriceDialog = priceInfo => {
  openEditDialog(
    factoryPieceRateInfoRef,
    priceInfo,
    data,
    'editPriceDialogMode',
    'editPriceDialogVisible'
  )
}

const resetPriceForm = () => {
  resetReactiveForm(factoryPieceRateInfoRef, factoryPieceRateInfoInit, priceInfoFormRef)
}

const closePriceDialog = () => {
  resetPriceForm()
  data.editPriceDialogVisible = false
}

const addPriceInfoRules = reactive({
  factoryId: [{ required: true, message: '请选择成衣厂', trigger: 'blur' }],
  styleNumber: [{ required: true, message: '请输入款式编号', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择工作类型', trigger: 'blur' }],
  price: [{ required: true, message: '请输入计件单价', trigger: 'blur' }]
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

.sample-upload-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
</style>
