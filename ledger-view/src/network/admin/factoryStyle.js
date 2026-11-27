import { request } from '../../base/request.js'

// 分页查询成衣厂款式
export function queryFactoryStyleList(factoryId, styleNumber, currentPage, pageSize, flag) {
  return request({
    url: '/admin/factoryStyle',
    method: 'get',
    params: {
      factoryId,
      styleNumber,
      currentPage,
      pageSize,
      flag
    }
  })
}

// 查询某个成衣厂下的款式下拉列表
export function queryStyleListByFactoryId(factoryId, flag = 0) {
  return request({
    url: '/admin/factoryStyle/byFactory',
    method: 'get',
    params: {
      factoryId,
      flag
    }
  })
}

// 保存成衣厂款式
export function saveFactoryStyleInfo(style) {
  return request({
    url: '/admin/factoryStyle',
    method: 'post',
    data: style
  })
}

// 修改成衣厂款式
export function updateFactoryStyleInfo(style) {
  return request({
    url: '/admin/factoryStyle/' + style.id,
    method: 'put',
    data: style
  })
}

// 删除成衣厂款式
export function deleteFactoryStyleInfo(id) {
  return request({
    url: '/admin/factoryStyle/' + id,
    method: 'delete'
  })
}

// 上传款式样板图片
export function uploadFactoryStyleImage(id, formData) {
  return request({
    url: `/admin/factoryStyle/${id}/image`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除款式样板图片
export function deleteFactoryStyleImage(id) {
  return request({
    url: `/admin/factoryStyle/${id}/image`,
    method: 'delete'
  })
}
