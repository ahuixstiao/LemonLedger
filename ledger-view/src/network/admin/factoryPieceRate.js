import { request } from '../../base/request.js'

// 按条件查询成衣厂计件单价列表
export function queryFactoryPieceRateList(factoryId, styleNumber, categoryId, currentPage, pageSize, flag) {
  return request({
    url: '/admin/factoryPieceRate',
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

// 按成衣厂ID查询成衣厂计件单价款式编号列表
export function queryFactoryPieceRateStyleNumberList(factoryId, flag) {
  return request({
    url: '/admin/factoryPieceRate/styleNumberList',
    method: 'get',
    params: {
      factoryId,
      flag
    }
  })
}

// 保存成衣厂计件单价信息
export function saveFactoryPieceRateInfo(price) {
  return request({
    url: '/admin/factoryPieceRate',
    method: 'post',
    data: price
  })
}

// 修改成衣厂计件单价信息
export function updateFactoryPieceRateInfo(price) {
  return request({
    url: '/admin/factoryPieceRate/' + price.id,
    method: 'put',
    data: price
  })
}

// 删除成衣厂计件单价信息
export function deleteFactoryPieceRateInfo(id) {
  return request({
    url: '/admin/factoryPieceRate/' + id,
    method: 'delete'
  })
}

// 上传成衣厂计件单价图片
export function uploadFactoryPieceRateImage(id, formData) {
  return request({
    url: `/admin/factoryPieceRate/${id}/image`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除成衣厂计件单价图片
export function deleteFactoryPieceRateImage(id) {
  return request({
    url: `/admin/factoryPieceRate/${id}/image`,
    method: 'delete'
  })
}
