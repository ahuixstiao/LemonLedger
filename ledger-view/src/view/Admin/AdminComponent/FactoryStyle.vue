<template>
  <div class="admin-page">
    <AdminPageHeader
      title="成衣厂款式"
      desc="款式编号与样板图片在这里统一维护；计件单价只引用款式，改图或改编号只用改这一处。"
    >
      <template #stats>
        <span class="admin-count-chip"><strong>{{ formatNumber(data.total) }}</strong> 个款式</span>
      </template>
      <template #actions>
        <el-button type="primary" @click="openCreateStyleDialog" title="Alt + A">
          <el-icon><Plus /></el-icon>
          添加款式
        </el-button>
      </template>
    </AdminPageHeader>

    <div class="filter-card">
      <div class="filter-bar">
        <el-select v-model="data.factoryId" class="filter-wide" placeholder="全部成衣厂" clearable @change="onFilterChange">
          <el-option v-for="item in data.factoryList" :key="item.id" :label="item.factoryName" :value="item.id" />
        </el-select>
        <el-input v-model="data.styleNumber" placeholder="搜索款式编号" clearable @keyup.enter="onSearch">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="data.flag" placeholder="正常" @change="onFilterChange">
          <el-option label="正常" :value="0" />
          <el-option label="已删除" :value="1" />
        </el-select>
        <div class="filter-actions">
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="resetStyleFilter">重置</el-button>
        </div>
      </div>
    </div>

    <div class="table-card">
      <el-table :data="data.tableData" empty-text="还没有款式，先添加一个" class="front-table" :height="'100%'">
        <el-table-column label="成衣厂" min-width="200" align="center">
          <template #default="scope">
            <span class="entity-cell">
              <span class="entity-avatar">{{ (scope.row.factoryName || '?').slice(0, 1) }}</span>
              {{ scope.row.factoryName || '—' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="styleNumber" label="款式编号" min-width="180" align="center" />
        <el-table-column label="已配价工种" width="130" align="center">
          <template #default="scope">
            <el-tooltip
              :content="scope.row.rateSummary || '还没有给这个款式配价'"
              placement="top"
            >
              <span :class="['num', { 'is-muted': !scope.row.rateCount }]">{{ formatNumber(scope.row.rateCount) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="样板图片" width="120" align="center">
          <template #default="scope">
            <el-image
              v-if="scope.row.imagePath"
              :src="resolveImageUrl(scope.row.imagePath)"
              :preview-src-list="[resolveImageUrl(scope.row.imagePath)]"
              fit="cover"
              class="style-thumb"
              preview-teleported
            />
            <span v-else class="muted">暂无</span>
          </template>
        </el-table-column>
        <el-table-column label="创建日期" width="130" align="center">
          <template #default="scope">
            <span class="date-cell">{{ formatDate(scope.row.createdDate) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="scope">
            <span class="status-pill" :class="scope.row.flag === 1 ? 'is-removed' : 'is-active'">
              {{ scope.row.flag === 1 ? '删除' : '正常' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="248" align="center" fixed="right">
          <template #default="scope">
            <template v-if="scope.row.flag === 0">
              <el-button link type="primary" @click="triggerStyleImageUpload(scope.row)">
                {{ scope.row.imagePath ? '换图片' : '传图片' }}
              </el-button>
              <el-button v-if="scope.row.imagePath" link type="danger" @click="removeStyleImage(scope.row)">删图</el-button>
              <el-button link type="primary" @click="openEditStyleDialog(scope.row)">编辑</el-button>
              <el-button link type="danger" @click="removeStyle(scope.row)">删除</el-button>
            </template>
            <span v-else class="muted">已删除，不可操作</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pager-row">
      <el-pagination
        v-model:current-page="data.currentPage"
        v-model:page-size="data.pageSize"
        :total="data.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        background
        @current-change="fetchStyleList"
        @size-change="onSizeChange"
      />
    </div>

    <input
      ref="styleImageUploadInputRef"
      type="file"
      accept="image/*"
      class="hidden-upload-input"
      @change="handleStyleImageUpload"
    />

    <el-dialog
      v-model="data.editStyleDialogVisible"
      :title="data.editStyleDialogMode === 0 ? '添加款式' : '修改款式'"
      width="420px"
    >
      <el-form
        ref="styleInfoFormRef"
        :model="styleInfoRef"
        :rules="styleRules"
        label-position="top"
      >
        <el-form-item label="成衣厂" prop="factoryId">
          <el-select filterable v-model="styleInfoRef.factoryId" placeholder="选择成衣厂">
            <el-option v-for="item in data.factoryList" :key="item.id" :label="item.factoryName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="款式编号" prop="styleNumber">
          <el-input ref="styleNumberInputRef" v-model="styleInfoRef.styleNumber" placeholder="输入款式编号" maxlength="30" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-hotkey-hint">Ctrl + Enter 提交 · Esc 关闭</span>
        <el-button @click="closeStyleDialog">取消</el-button>
        <el-button type="primary" @click="validateStyleForm(styleInfoFormRef)">
          {{ data.editStyleDialogMode === 0 ? '新增' : '保存' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import AdminPageHeader from '../AdminPageHeader.vue'
import { confirmDelete, formatDate, formatNumber } from '../adminShared.js'
import { useDialogShortcuts } from '../../../base/dialogShortcuts.js'
import { queryFactoryList } from '../../../network/index.js'
import {
  deleteFactoryStyleInfo,
  deleteFactoryStyleImage,
  queryFactoryStyleList,
  saveFactoryStyleInfo,
  updateFactoryStyleInfo,
  uploadFactoryStyleImage
} from '../../../network/admin/index.js'
import {
  loadFactoryOptions,
  openCreateDialog,
  openEditDialog,
  resetReactiveForm,
  validateDialogForm
} from './factoryCrudShared.js'

const data = reactive({
  tableData: [],
  factoryList: [],
  factoryId: '',
  styleNumber: '',
  flag: 0,
  currentPage: 1,
  pageSize: 10,
  total: 0,
  uploadingStyleId: '',
  editStyleDialogVisible: false,
  editStyleDialogMode: 0,
  imageVersion: 0
})

onMounted(() => {
  loadFactoryList()
  fetchStyleList()
  document.addEventListener('keydown', onStyleListHotkey, true)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', onStyleListHotkey, true)
})

const loadFactoryList = async () => {
  await loadFactoryOptions(data, queryFactoryList)
}

const fetchStyleList = async () => {
  const { data: res } = await queryFactoryStyleList(
    data.factoryId,
    data.styleNumber,
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
  fetchStyleList()
}

const onFilterChange = () => {
  data.currentPage = 1
  fetchStyleList()
}

const onSizeChange = () => {
  data.currentPage = 1
  fetchStyleList()
}

const resetStyleFilter = () => {
  data.factoryId = ''
  data.styleNumber = ''
  data.flag = 0
  data.currentPage = 1
  fetchStyleList()
}

// 列表页按 Alt + A 直接打开「添加款式」
const onStyleListHotkey = event => {
  if (event.altKey && (event.key === 'a' || event.key === 'A')) {
    const tag = (event.target?.tagName || '').toUpperCase()
    if (tag === 'INPUT' || tag === 'TEXTAREA') return
    event.preventDefault()
    openCreateStyleDialog()
  }
}

const styleInfoInit = {
  id: '',
  factoryId: '',
  styleNumber: '',
  imagePath: ''
}

const styleInfoRef = reactive({ ...styleInfoInit })
const styleInfoFormRef = ref()
const styleNumberInputRef = ref()
const styleImageUploadInputRef = ref(null)

const openCreateStyleDialog = () => {
  openCreateDialog(
    styleInfoRef,
    styleInfoInit,
    data,
    'editStyleDialogMode',
    'editStyleDialogVisible'
  )
  nextTick(() => styleNumberInputRef.value?.focus?.())
}

const openEditStyleDialog = style => {
  openEditDialog(styleInfoRef, style, data, 'editStyleDialogMode', 'editStyleDialogVisible')
}

const closeStyleDialog = () => {
  resetReactiveForm(styleInfoRef, styleInfoInit, styleInfoFormRef)
  data.editStyleDialogVisible = false
}

const createStyle = async () => {
  const { data: res } = await saveFactoryStyleInfo(styleInfoRef)
  if (res.status === 200) {
    ElMessage.success(res.message || '添加款式成功')
    data.editStyleDialogVisible = false
    resetReactiveForm(styleInfoRef, styleInfoInit, styleInfoFormRef)
    await fetchStyleList()
  } else {
    ElMessage.error(res.message)
  }
}

const updateStyle = async () => {
  const { data: res } = await updateFactoryStyleInfo(styleInfoRef)
  if (res.status === 200) {
    ElMessage.success(res.message || '修改款式成功')
    data.editStyleDialogVisible = false
    await fetchStyleList()
  } else {
    ElMessage.error(res.message)
  }
}

const validateStyleForm = async formEl => {
  await validateDialogForm(
    formEl,
    data.editStyleDialogMode,
    createStyle,
    updateStyle,
    () => ElMessage.error('请检查是否填写正确')
  )
}

const removeStyle = async row => {
  const confirmed = await confirmDelete(`确定删除「${row.factoryName || ''} · ${row.styleNumber}」这个款式吗？`)
  if (!confirmed) return

  const { data: res } = await deleteFactoryStyleInfo(row.id)
  if (res.status === 200) {
    ElMessage.success(res.message)
    await fetchStyleList()
  } else {
    ElMessage.error(res.message)
  }
}

const triggerStyleImageUpload = style => {
  data.uploadingStyleId = style.id
  if (styleImageUploadInputRef.value) {
    styleImageUploadInputRef.value.value = ''
    styleImageUploadInputRef.value.click()
  }
}

const handleStyleImageUpload = async event => {
  const target = event?.target
  const file = target?.files?.[0]

  if (!file) return

  if (!data.uploadingStyleId) {
    ElMessage.error('未识别到对应款式，请重试')
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
    const { data: res } = await uploadFactoryStyleImage(data.uploadingStyleId, formData)
    if (res.status === 200) {
      ElMessage.success(res.message || '上传成功')
      data.imageVersion += 1
      await fetchStyleList()
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (error) {
    ElMessage.error('上传失败，请稍后重试')
  } finally {
    data.uploadingStyleId = ''
    target.value = ''
  }
}

const resolveImageUrl = imagePath => {
  if (!imagePath) return ''

  const normalizedPath = String(imagePath)
  if (normalizedPath.startsWith('http')) return normalizedPath

  const baseUrl = (import.meta.env.VITE_BASE_URL || '').replace(/\/$/, '')
  const imagePrefix = '/lemonLedgerImages/'

  if (normalizedPath.startsWith(imagePrefix)) {
    const relativePath = normalizedPath.slice(imagePrefix.length)
    return `${baseUrl}/admin/factoryStyle/imageView/${relativePath}?v=${data.imageVersion}`
  }

  return `${baseUrl}${normalizedPath}?v=${data.imageVersion}`
}

const styleRules = reactive({
  factoryId: [{ required: true, message: '请选择成衣厂', trigger: 'change' }],
  styleNumber: [{ required: true, message: '请输入款式编号', trigger: 'blur' }]
})

useDialogShortcuts({
  isOpen: () => data.editStyleDialogVisible,
  onConfirm: () => validateStyleForm(styleInfoFormRef.value),
  onCancel: closeStyleDialog
})

// 删除某个款式的样板图片（只删图，款式本身保留）
const removeStyleImage = async row => {
  const confirmed = await confirmDelete('确定删除这张样板图片吗？')
  if (!confirmed) return

  const { data: res } = await deleteFactoryStyleImage(row.id)
  if (res.status === 200) {
    ElMessage.success(res.message || '删除图片成功')
    data.imageVersion += 1
    await fetchStyleList()
  } else {
    ElMessage.error(res.message || '删除图片失败')
  }
}
</script>

<style scoped>
.hidden-upload-input {
  display: none;
}

.num.is-muted {
  color: var(--admin-text-muted, #98a4bb);
  text-decoration: underline dotted;
}

.style-thumb {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  border: 1px solid #e9eef7;
  cursor: zoom-in;
}
</style>
