import { request } from '../base/request.js'

// 查询员工列表
export function queryEmployees() {
  return request({
    url: '/emp/list',
    method: 'get'
  })
}

// 查询工作方式列表
export function queryModeList() {
  return request({
    url: '/emp/mode/list',
    method: 'get'
  })
}
