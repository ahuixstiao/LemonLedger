import { request } from '../../base/request.js'

export function queryJobCategoryList(currentPage, pageSize, category, flag = 0) {
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
