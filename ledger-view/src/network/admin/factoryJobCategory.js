import { request } from '../../base/request.js'

// 查询成衣厂工作类型列表
export function queryFactoryJobCategoryList() {
  return request({
    url: '/admin/factoryJobCategory/list',
    method: 'get'
  })
}

// 新增成衣厂工作类型
export function saveFactoryJobCategory(data) {
  return request({
    url: '/admin/factoryJobCategory',
    method: 'post',
    data
  })
}

// 修改成衣厂工作类型
export function updateFactoryJobCategory(data) {
  return request({
    url: '/admin/factoryJobCategory',
    method: 'put',
    data
  })
}

// 删除成衣厂工作类型
export function deleteFactoryJobCategory(id) {
  return request({
    url: '/admin/factoryJobCategory/' + id,
    method: 'delete'
  })
}
