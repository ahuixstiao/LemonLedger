import { request } from '../../base/request.js'
import { normalizeDateParam, normalizeDateRangeParams } from '../dateParam.js'

// 按条件查询成衣厂账单列表
export function queryFactoryBillList(
  factoryId,
  number,
  styleNumber,
  category,
  flag,
  startDate,
  endDate,
  currentPage,
  pageSize
) {
  return request({
    url: '/admin/factoryBill',
    method: 'get',
    params: {
      factoryId,
      number,
      styleNumber,
      category,
      flag,
      startDate: normalizeDateParam(startDate),
      endDate: normalizeDateParam(endDate),
      currentPage,
      pageSize
    }
  })
}

// 保存成衣厂账单
// confirmDuplicate：已经确认「这一床确实是返工/补做，就是要再记一次」时传 true，跳过重复校验
export function saveFactoryBillInfo(bill, confirmDuplicate = false) {
  return request({
    url: '/admin/factoryBill',
    method: 'post',
    data: bill,
    params: { confirmDuplicate }
  })
}

// 统计指定成衣厂账单
export function statisticalFactoryBill(factoryId, startDate, endDate) {
  return request({
    url: '/admin/factoryBill/bill/' + factoryId,
    method: 'get',
    params: normalizeDateRangeParams(startDate, endDate)
  })
}

// 导出成衣厂账单
export function exportFactoryBillExcel(factoryId, startDate, endDate, sortConfigList = []) {
  const dateParams = normalizeDateRangeParams(startDate, endDate)
  const sortFields = sortConfigList.map(item => item.field).join(',')
  const sortOrders = sortConfigList.map(item => item.order).join(',')

  return request({
    url: '/admin/factoryBill/excel/' + factoryId,
    method: 'get',
    responseType: 'blob',
    params: {
      ...dateParams,
      sortFields,
      sortOrders
    }
  })
}

// 修改成衣厂账单
// confirmDuplicate 同上：确认过重复也要保存时传 true
export function editFactoryBillInfo(bill, confirmDuplicate = false) {
  return request({
    url: '/admin/factoryBill/' + bill.id,
    method: 'put',
    data: bill,
    params: { confirmDuplicate }
  })
}

// 删除成衣厂账单
export function deleteFactoryBillInfo(id) {
  return request({
    url: '/admin/factoryBill/' + id,
    method: 'delete'
  })
}

// 按成衣厂统计毛利（账单收入 - 工资支出）
// 日期都不传时后端按「本月 1 号到今天」
export function queryFactoryGrossProfit(startDate, endDate) {
  return request({
    url: '/admin/factoryBill/gross',
    method: 'get',
    params: { startDate, endDate }
  })
}
