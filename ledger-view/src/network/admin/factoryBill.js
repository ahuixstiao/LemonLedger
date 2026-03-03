import { request } from '../../base/request.js'

// 按条件查询成衣厂账单列表
export function queryFactoryBillList(
  factoryId,
  number,
  styleNumber,
  categoryId,
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
      categoryId,
      flag,
      startDate,
      endDate,
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
    params: { startDate, endDate }
  })
}

// 导出成衣厂账单
export function exportFactoryBillExcel(factoryId, startDate, endDate) {
  return request({
    url: '/admin/factoryBill/excel/' + factoryId,
    method: 'get',
    responseType: 'blob',
    params: { startDate, endDate }
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
