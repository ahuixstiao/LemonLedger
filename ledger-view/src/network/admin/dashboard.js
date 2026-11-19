import { request } from '../../base/request.js'

/**
 * 首页看板汇总：账单、工资、毛利、对账情况与每日趋势。
 *
 * @param {string} startDate yyyy-MM-dd
 * @param {string} endDate   yyyy-MM-dd
 * @returns {Promise}
 */
export function queryDashboardSummary(startDate, endDate) {
  return request({
    url: '/admin/dashboard/summary',
    method: 'get',
    params: { startDate, endDate }
  })
}
