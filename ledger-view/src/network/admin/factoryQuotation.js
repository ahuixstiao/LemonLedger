import { request } from '../../base/request.js'

// 按条件查询成衣厂报价单列表
export function queryFactoryQuotationList(factoryId, styleNumber, categoryId, currentPage, pageSize, flag) {
  return request({
    url: '/admin/factoryQuotation',
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
    url: '/admin/factoryQuotation',
    method: 'post',
    data: quotation
  })
}

// 修改成衣厂报价单信息
export function updateFactoryQuotationInfo(quotation) {
  return request({
    url: '/admin/factoryQuotation/' + quotation.id,
    method: 'put',
    data: quotation
  })
}

// 删除成衣厂报价单信息
export function deleteFactoryQuotationInfo(id) {
  return request({
    url: '/admin/factoryQuotation/' + id,
    method: 'delete'
  })
}

// 上传成衣厂报价单图片
export function uploadFactoryQuotationImage(id, formData) {
  return request({
    url: `/admin/factoryQuotation/${id}/image`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除成衣厂报价单图片
export function deleteFactoryQuotationImage(id) {
  return request({
    url: `/admin/factoryQuotation/${id}/image`,
    method: 'delete'
  })
}
