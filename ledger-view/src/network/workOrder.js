import { request } from '../base/request.js'
import { normalizeDateRangeParams } from './dateParam.js'

// 按条件查询工单列表
export function queryWorkOrderList(
  factoryId,
  number,
  styleNumber,
  categoryId,
  startDate,
  endDate,
  currentPage,
  pageSize,
  flag = 0
) {
  return request({
    url: '/emp/workOrder',
    method: 'get',
    params: {
      factoryId,
      number,
      styleNumber,
      categoryId,
      ...normalizeDateRangeParams(startDate, endDate),
      currentPage,
      pageSize,
      flag
    }
  })
}

// 新增工单
export function saveWorkOrder(workOrder) {
  return request({
    url: '/emp/workOrder',
    method: 'post',
    data: workOrder
  })
}

// 修改工单
export function updateWorkOrder(workOrder) {
  return request({
    url: '/emp/workOrder/' + workOrder.id,
    method: 'put',
    data: workOrder
  })
}

// 删除工单
export function deleteWorkOrder(id) {
  return request({
    url: '/emp/workOrder/' + id,
    method: 'delete'
  })
}
