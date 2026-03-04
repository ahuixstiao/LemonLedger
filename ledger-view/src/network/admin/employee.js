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

// 管理端编辑员工
export function updateAdminEmployee(employee) {
  return request({
    url: `/admin/emp/${employee.id}`,
    method: 'put',
    data: employee
  })
}

// 管理端删除员工
export function deleteAdminEmployee(id) {
  return request({
    url: `/admin/emp/${id}`,
    method: 'delete'
  })
}
