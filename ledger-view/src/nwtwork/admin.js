import { request } from './request.js'

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
    url: '/admin/factoryBill/list',
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
    url: '/admin/factoryBill/save',
    method: 'post',
    data: bill
  })
}

// 统计指定成衣厂账单
export function statisticalFactoryBill(factoryId, startDate, endDate) {
  return request({
    url: '/admin/factoryBill/bill/' + factoryId,
    method: 'get',
    params: {
      startDate,
      endDate
    }
  })
}

// 导出成衣厂账单
export function exportFactoryBill(billId) {
    return request({

    })
}

// 修改成衣厂账单
export function editFactoryBillInfo(bill) {
  return request({
    url: '/admin/factoryBill/update',
    method: 'put',
    data: bill
  })
}

// 删除成衣厂账单
export function deleteFactoryBillInfo(id) {
    return request({
        url: '/admin/factoryBill/delete/' + id,
        method: 'delete'
    })
}


// 按条件查询成衣厂报价单列表
export function queryFactoryQuotationList(factoryId, styleNumber, categoryId, currentPage, pageSize, flag) {
    return request({
        url: '/admin/factoryQuotation/list',
        method: 'get',
        params: {
            factoryId,
            styleNumber,
            categoryId,
            currentPage,
            pageSize,
            flag
        }
    })
}

// 按成衣厂ID查询成衣厂报价单款式编号列表
export function queryFactoryQuotationStyleNumberList(factoryId, flag) {
    return request({
        url: '/admin/factoryQuotation/styleNumberList',
        method: 'get',
        params: {
            factoryId,
            flag
        }
    })
}


// 保存成衣厂报价单信息
export function saveFactoryQuotationInfo(quotation) {
    return request({
        url: '/admin/factoryQuotation/save',
        method: 'post',
        data: quotation
    })
}

// 修改成衣厂报价单信息
export function updateFactoryQuotationInfo(quotation) {
    return request({
        url: '/admin/factoryQuotation/update',
        method: 'put',
        data: quotation
    })
}

// 删除成衣厂报价单信息
export function deleteFactoryQuotationInfo(id) {
    return request({
        url: '/admin/factoryQuotation/delete/' + id,
        method: 'delete'
    })
}
