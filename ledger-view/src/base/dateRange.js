// 工单类列表的默认时间窗。
// 后端在没传开始日期时只查「当天」，当天没有工单时页面一进来就是空的，
// 很容易被当成“查不到工单”，所以前台默认给一个最近 N 天的范围。
const pad = value => String(value).padStart(2, '0')

export const toDateValue = date =>
  `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`

export const recentDateRange = (days = 30) => {
  const end = new Date()
  const start = new Date(end)
  start.setDate(start.getDate() - (Math.max(days, 1) - 1))
  return { startDate: toDateValue(start), endDate: toDateValue(end) }
}
