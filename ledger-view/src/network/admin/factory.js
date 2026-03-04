import { request } from '../../base/request.js'

// 管理端按条件查询成衣厂列表
export function queryAdminFactoryList(factoryName, currentPage, pageSize, flag) {
  return request({
    url: '/admin/factory',
    method: 'get',
    params: {
      factoryName,
      currentPage,
      pageSize,
      flag
    }
  })
}

// 管理端保存成衣厂
export function saveAdminFactory(factory) {
  return request({
    url: '/admin/factory',
    method: 'post',
    data: factory
  })
}
