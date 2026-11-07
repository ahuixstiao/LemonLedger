import { request } from '../../base/request.js'

// 查询全部工作类型（下拉框用：成衣厂计件单价、员工计件单价、账单录入共用这一份字典）
export function queryJobCategoryList() {
  return request({
    url: '/admin/jobCategory/list',
    method: 'get'
  })
}

export function queryJobCategoryListCondition(currentPage, pageSize, category, flag = 0) {
  return request({
    url: '/admin/jobCategory',
    method: 'get',
    params: { currentPage, pageSize, category, flag }
  })
}

export function saveJobCategory(data) {
  return request({
    url: '/admin/jobCategory',
    method: 'post',
    data
  })
}

export function deleteJobCategory(id) {
  return request({
    url: '/admin/jobCategory/' + id,
    method: 'delete'
  })
}
