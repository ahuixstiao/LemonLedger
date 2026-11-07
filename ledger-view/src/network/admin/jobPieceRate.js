import { request } from '../../base/request.js'

export function queryJobPieceRateList(modeId, categoryId, currentPage, pageSize, flag) {
  return request({
    url: '/admin/jobPieceRate',
    method: 'get',
    params: { modeId, categoryId, currentPage, pageSize, flag }
  })
}

export function saveJobPieceRate(data) {
  return request({
    url: '/admin/jobPieceRate',
    method: 'post',
    data
  })
}

export function updateJobPieceRate(data) {
  return request({
    url: '/admin/jobPieceRate/' + data.id,
    method: 'put',
    data
  })
}

export function deleteJobPieceRate(id) {
  return request({
    url: '/admin/jobPieceRate/' + id,
    method: 'delete'
  })
}
