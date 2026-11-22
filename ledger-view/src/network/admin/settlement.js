import { request } from '../../base/request.js'
import { normalizeDateRangeParams } from '../dateParam.js'

/**
 * 结算单分页列表。
 *
 * @param {{factoryId?:number|string,status?:number|string,startDate?:string,endDate?:string,currentPage?:number,pageSize?:number}} params
 * @returns {Promise}
 */
export function querySettlementList(params = {}) {
  return request({
    url: '/admin/settlement',
    method: 'get',
    params: {
      factoryId: params.factoryId ?? '',
      status: params.status ?? '',
      ...normalizeDateRangeParams(params.startDate ?? '', params.endDate ?? ''),
      currentPage: params.currentPage ?? 1,
      pageSize: params.pageSize ?? 10
    }
  })
}

/**
 * 结算单明细。
 *
 * @param {number} id 结算单ID
 * @returns {Promise}
 */
export function querySettlementItems(id) {
  return request({
    url: `/admin/settlement/${id}/items`,
    method: 'get'
  })
}

/**
 * 登记结清。
 *
 * @param {number} id 结算单ID
 * @param {{paidAmount:number,paidDate?:string,remark?:string}} payload
 * @returns {Promise}
 */
export function settleSettlement(id, payload) {
  return request({
    url: `/admin/settlement/${id}/settle`,
    method: 'put',
    data: payload
  })
}

/**
 * 作废结算单。
 *
 * @param {number} id 结算单ID
 * @returns {Promise}
 */
export function voidSettlement(id) {
  return request({
    url: `/admin/settlement/${id}`,
    method: 'delete'
  })
}

/**
 * 按快照重新导出 Excel。
 *
 * @param {number} id 结算单ID
 * @returns {Promise}
 */
export function exportSettlementExcel(id) {
  return request({
    url: `/admin/settlement/${id}/excel`,
    method: 'get',
    responseType: 'blob'
  })
}
