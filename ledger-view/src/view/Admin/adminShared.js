import { ElMessageBox } from 'element-plus'

const moneyFormatter = new Intl.NumberFormat('zh-CN', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
})

const numberFormatter = new Intl.NumberFormat('zh-CN')

/**
 * 金额统一展示：千分位 + 两位小数 + 前置 ¥。
 * 空值或非数字返回占位符，避免列表里出现 NaN / null。
 *
 * @param {number|string} value 金额
 * @returns {string} 展示文本
 */
export function formatMoney(value) {
  if (value === null || value === undefined || value === '') return '—'
  const number = Number(value)
  if (Number.isNaN(number)) return '—'
  return `¥${moneyFormatter.format(number)}`
}

/**
 * 数量/计数统一展示：千分位，整数。
 *
 * @param {number|string} value 数值
 * @returns {string} 展示文本
 */
export function formatNumber(value) {
  if (value === null || value === undefined || value === '') return '—'
  const number = Number(value)
  if (Number.isNaN(number)) return '—'
  return numberFormatter.format(number)
}

/**
 * 日期统一展示成 2026/11/07，空值给占位符。
 *
 * @param {string} value 日期字符串
 * @returns {string} 展示文本
 */
export function formatDate(value) {
  if (!value) return '—'
  return String(value).replaceAll('-', '/')
}

/**
 * 删除前统一确认，防止列表里一次误点就逻辑删除。
 *
 * @param {string} message 确认说明
 * @returns {Promise<boolean>} 是否确认删除
 */
export async function confirmDelete(message = '删除后该记录会进入「删除」状态，可在筛选里查看，确定删除吗？') {
  try {
    await ElMessageBox.confirm(message, '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
    return true
  } catch {
    return false
  }
}

/**
 * 工作方式标签配色：压花/刮胶是业务里固定的两种，未知的走中性色。
 *
 * @param {number|string} modeId 工作方式 ID
 * @returns {'primary'|'warning'|'info'} el-tag 类型
 */
export function modeTagType(modeId) {
  if (Number(modeId) === 1) return 'primary'
  if (Number(modeId) === 2) return 'warning'
  return 'info'
}
