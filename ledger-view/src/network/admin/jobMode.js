import { request } from '../../base/request.js'

export function queryJobModeList(currentPage, pageSize, mode, flag = 0) {
  return request({
    url: '/admin/jobMode',
    method: 'get',
    params: { currentPage, pageSize, mode, flag }
  })
}

export function saveJobMode(data) {
  return request({
    url: '/admin/jobMode',
    method: 'post',
    data
  })
}

export function deleteJobMode(id) {
  return request({
    url: '/admin/jobMode/' + id,
    method: 'delete'
  })
}
