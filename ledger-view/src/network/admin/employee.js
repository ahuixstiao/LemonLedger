import { request } from '../../base/request.js'

// 管理端查询员工列表（支持名称筛选与分页）
export function queryAdminEmployeeList(name, currentPage, pageSize, flag) {
  return request({
    url: '/admin/emp',
    method: 'get',
    params: { name, currentPage, pageSize, flag }
  })
}

// 管理端保存员工
export function saveAdminEmployee(employee) {
  return request({
    url: '/admin/emp',
    method: 'post',
    data: employee
  })
}
