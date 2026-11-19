import { request } from '../../base/request.js'
import { normalizeDateRangeParams } from '../dateParam.js'

// 员工产能排行：区间内按员工汇总件数与工资
// 日期都不传时后端按「最近 30 天」
export function queryEmployeeRank(factoryId, startDate, endDate) {
  return request({
    url: '/admin/rank/employees',
    method: 'get',
    params: { factoryId, ...normalizeDateRangeParams(startDate, endDate) }
  })
}
