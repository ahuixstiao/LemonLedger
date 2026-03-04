/**
 * 规范化日期查询参数。
 * - null/undefined/空串/"0" 统一返回 undefined（axios 不会拼接该参数）
 * - 其余值返回 trim 后字符串
 */
export function normalizeDateParam(value) {
  if (value === null || value === undefined) return undefined
  const trimmed = String(value).trim()
  if (!trimmed || trimmed === '0') return undefined
  return trimmed
}

/**
 * 规范化日期区间参数对象。
 */
export function normalizeDateRangeParams(startDate, endDate) {
  return {
    startDate: normalizeDateParam(startDate),
    endDate: normalizeDateParam(endDate)
  }
}
