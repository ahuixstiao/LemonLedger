import { request } from '../base/request.js'

// 查询工厂列表
export function queryFactoryList() {
  return request({
    url: '/emp/factory/list',
    method: 'get'
  })
}
