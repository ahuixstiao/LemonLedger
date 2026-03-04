import { request } from '../../base/request.js'
import { normalizeDateParam, normalizeDateRangeParams } from '../dateParam.js'

// 按条件查询成衣厂账单列表
export function queryFactoryBillList(
  factoryId,
  number,
  styleNumber,
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
      flag,
      startDate: normalizeDateParam(startDate),
      endDate: normalizeDateParam(endDate),
      currentPage,
      pageSize
    }
  })
}

// 保存成衣厂账单
export function saveFactoryBillInfo(bill) {
  return request({
    url: '/admin/factoryBill',
    method: 'post',
    data: bill
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
export function editFactoryBillInfo(bill) {
  return request({
    url: '/admin/factoryBill/' + bill.id,
    method: 'put',
    data: bill
  })
}

// 删除成衣厂账单
export function deleteFactoryBillInfo(id) {
  return request({
    url: '/admin/factoryBill/' + id,
    method: 'delete'
  })
}
