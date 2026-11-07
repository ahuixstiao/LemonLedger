<template>
  <div class="factoryPieceRate-container">
    <!-- 筛选条件 -->
    <div class="factoryPieceRate-button">
      <el-button type="primary" @click="openCreatePriceDialog">添加</el-button>

      <!-- 成衣厂筛选条件 -->
      <el-select
          v-model="data.factoryId"
          filterable
          clearable
          placeholder="选择工厂名称"
          @change="fetchFactoryPieceRateList"
      >
        <el-option
            v-for="item in data.factoryList"
            :label="item.factoryName"
            :value="item.id"
        />
      </el-select>

      <!-- 款式编号筛选条件 -->
      <el-input
          clearable
          v-model="data.styleNumber"
          placeholder="款式编号"
          @input="fetchFactoryPieceRateList"
      >
      </el-input>

      <!-- 工作类型筛选条件 -->
      <el-select
          v-model="data.categoryId"
          clearable
          placeholder="选择工作类型"
          @change="fetchFactoryPieceRateList"
      >
        <el-option
            v-for="item in data.categoryList"
            :label="item.category"
            :value="item.id"
        />
      </el-select>

      <!-- 状态筛选条件 -->
      <el-select
          v-model="data.flag"
          placeholder="状态"
          @change="fetchFactoryPieceRateList"
      >
        <el-option label="正常" :value="0" />
        <el-option label="删除" :value="1" />
      </el-select>

      <el-button type="warning" plain class="toolbar-reset-btn" @click="resetPriceFilter">重置</el-button>
    </div>

    <!-- 成衣厂计件单价表格 -->
    <el-table
        :data="data.tableData"
        style="width: 100%"
        stripe
        fit
        highlight-current-row
        empty-text="暂无数据"
    >
      <el-table-column prop="factoryName" sortable label="厂名" align="center"/>
      <el-table-column prop="styleNumber" sortable label="款式编号" align="center"/>
      <el-table-column prop="category" sortable label="工作类型" align="center"/>
      <el-table-column prop="price" sortable label="计件单价 (元)" align="center"/>
      <el-table-column label="样板" align="center" width="100">
        <template #default="scope">
          <el-image
            v-if="scope.row.imagePath"
            :src="resolveImageUrl(scope.row.imagePath)"
            :preview-src-list="[resolveImageUrl(scope.row.imagePath)]"
            fit="cover"
            class="sample-image"
            preview-teleported
          />
          <span v-else class="readonly-text">暂无</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdDate" sortable label="创建日期" align="center"/>
      <el-table-column prop="flag" sortable label="状态" align="center" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.flag === 0" type="success">正常</el-tag>
          <el-tag v-else type="danger">删除</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="220px">
        <template #default="scope">
          <template v-if="isReadOnlyRow(scope.row)">
            <span class="readonly-text">已删除</span>
          </template>
          <template v-else>
            <el-button text @click="openEditPriceDialog(scope.row)">编辑</el-button>
            <el-button type="danger" text @click="removeFactoryPieceRate(scope.row.id)">删除</el-button>
          </template>
        </template>
      </el-table-column>

    </el-table>

    <input
      ref="priceImageUploadInputRef"
      type="file"
      accept="image/*"
      class="hidden-upload-input"
      @change="handlePriceImageUpload"
    />

    <!-- 分页组件 -->
    <div class="factoryPieceRate-page">
      <el-pagination
          background
          :total="data.total"
          v-model:current-page="data.currentPage"
          v-model:page-size="data.pageSize"
          :page-sizes="[5, 10, 20, 50, 100]"
          layout="sizes, prev, pager, next, jumper, ->"
          @current-change="fetchFactoryPieceRateList"
          @size-change="fetchFactoryPieceRateList"
      />
    </div>

    <!--  添加或编辑成衣厂账单信息弹窗  -->
    <el-dialog
        v-model="data.editPriceDialogVisible"
        :title="
        data.editPriceDialogMode === 0 ? '添加成衣厂计件单价' : data.editPriceDialogMode === 1 ? '修改成衣厂计件单价': ''"
        width="50%"
        center
    >
      <el-form
          ref="priceInfoFormRef"
          :model="factoryPieceRateInfoRef"
          :rules="addPriceInfoRules"
          label-width="auto"
          label-position="left"
      >
        <el-form-item size="large" label="工厂名称:" prop="factoryId">
          <el-select
              filterable
              v-model="factoryPieceRateInfoRef.factoryId"
              placeholder="选择工厂名称"
          >
            <el-option
                v-for="item in data.factoryList"
                :label="item.factoryName"
                :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item size="large" label="款式编号:" prop="styleNumber">
          <el-input
              v-model="factoryPieceRateInfoRef.styleNumber"
              placeholder="输入款式编号"
          />
        </el-form-item>

        <el-form-item size="large" label="工作类型:" prop="categoryId">
          <el-select
              v-model="factoryPieceRateInfoRef.categoryId"
              placeholder="选择工作类型"
          >
            <el-option
                v-for="item in data.categoryList"
                :label="item.category"
                :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item size="large" label="计件单价:" prop="price">
          <el-input
              v-model="factoryPieceRateInfoRef.price"
              placeholder="输入计件单价"
          />
        </el-form-item>

        <el-form-item v-if="data.editPriceDialogMode === 1" size="large" label="样板图片:">
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
          <span v-else class="readonly-text">当前单价单暂无图片</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closePriceDialog">取消</el-button>
          <el-button type="primary" v-if="data.editPriceDialogMode === 0"
                     @click="validatePriceForm(priceInfoFormRef)">
            新增
          </el-button>
          <el-button type="primary"
                     v-if="data.editPriceDialogMode === 1" @click="validatePriceForm(priceInfoFormRef)">
            修改
          </el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>


<script setup>

import { onMounted, reactive, ref } from 'vue'
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
import { ElMessage } from 'element-plus'
import {
  loadFactoryOptions,
  loadFactoryCategoryOptions,
  openCreateDialog,
  openEditDialog,
  resetReactiveForm,
  validateDialogForm
} from './factoryCrudShared.js'


onMounted(() => {
  loadFactoryList()
  loadCategoryList()
  fetchFactoryPieceRateList()
})

const data = reactive({
  // 表格数据
  tableData: [],

  factoryList: [],
  categoryList: [],

  factoryId: '',
  number: '',
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

// 加载成衣厂选项
const loadFactoryList = async () => {
  await loadFactoryOptions(data, queryFactoryList)
}

// 加载工作类型选项
const loadCategoryList = async () => {
  await loadFactoryCategoryOptions(data, queryJobCategoryList)
}

// TODO 查询成衣厂计件单价列表
const fetchFactoryPieceRateList = async () => {

  const {data: res} = await queryFactoryPieceRateList(
      data.factoryId,
      data.styleNumber,
      data.categoryId,
      data.currentPage,
      data.pageSize,
      data.flag
  )
  // 判断请求是否成功
  if (res.status === 200) {
    data.tableData = res.data.records
    data.total = res.data.total
  } else {
    data.tableData = []
  }

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
    ElMessage.success(res.message)
    data.editPriceDialogVisible = false
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

const removeFactoryPieceRate = async id => {
  const { data: res } = await deleteFactoryPieceRateInfo(id)
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

// 构建计件单价单实体
const factoryPieceRateInfoRef = reactive({...factoryPieceRateInfoInit})


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

// 添加账单记录校验规则
const addPriceInfoRules = reactive({
  factoryId: [{required: true, message: '请选择厂名', trigger: 'blur'}],
  styleNumber: [{required: true, message: '请输入款式编号', trigger: 'blur'}],
  categoryId: [{required: true, message: '请选择工作类型', trigger: 'blur'}],
  price: [{required: true, message: '请输入计件单价', trigger: 'blur'}],
})

</script>

<style scoped>
.factoryPieceRate-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.factoryPieceRate-button {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  padding: 12px;
  gap: 12px;
  border-bottom: 1px solid var(--el-border-color);
  overflow-x: auto;
  overflow-y: hidden;
}

.factoryPieceRate-button > *:nth-child(1) {
  width: 96px;
}

.factoryPieceRate-button > *:nth-child(2) {
  width: 200px;
}

.factoryPieceRate-button > *:nth-child(3) {
  width: 180px;
}

.factoryPieceRate-button > *:nth-child(4) {
  width: 160px;
}

.factoryPieceRate-button > *:nth-child(5) {
  width: 110px;
}

.factoryPieceRate-button > *:nth-child(6) {
  width: 96px;
}

.factoryPieceRate-button > * {
  flex-shrink: 0;
}

.factoryPieceRate-button :deep(.el-input__wrapper),
.factoryPieceRate-button :deep(.el-select__wrapper) {
  min-height: 32px;
}

.factoryPieceRate-page {
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  padding: 25px 0;
}

:deep(.el-form-item__content) {
  flex-wrap: nowrap;
  gap: 10px;
}

:deep(.el-table--fit) {
  flex: auto;
}

.readonly-text {
  color: var(--el-text-color-placeholder);
}

.hidden-upload-input {
  display: none;
}

.sample-image {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  border: 1px solid var(--el-border-color-light);
}

</style>