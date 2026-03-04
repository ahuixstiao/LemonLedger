import { request } from '../../base/request.js'

export function queryJobCategoryList(category, currentPage, pageSize, flag) {
  return request({
    url: '/admin/jobCategory',
    method: 'get',
    params: { category, currentPage, pageSize, flag }
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
