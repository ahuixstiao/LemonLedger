import { request } from '../../base/request.js'

// 查询成衣厂工作类型列表
export function queryFactoryJobCategoryList() {
  return request({
    url: '/admin/factoryJobCategory/list',
    method: 'get'
  })
}
