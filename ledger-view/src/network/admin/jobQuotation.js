import { request } from '../../base/request.js'

export function queryJobQuotationList(modeId, categoryId, currentPage, pageSize, flag) {
  return request({
    url: '/admin/jobQuotation',
    method: 'get',
    params: { modeId, categoryId, currentPage, pageSize, flag }
  })
}

export function saveJobQuotation(data) {
  return request({
    url: '/admin/jobQuotation',
    method: 'post',
    data
  })
}

export function updateJobQuotation(data) {
  return request({
    url: '/admin/jobQuotation/' + data.id,
    method: 'put',
    data
  })
}

export function deleteJobQuotation(id) {
  return request({
    url: '/admin/jobQuotation/' + id,
    method: 'delete'
  })
}
