import { request } from '../base/request.js'
import { normalizeDateRangeParams } from './dateParam.js'

// 添加工作记录
export function saveJobInfo(job) {
  return request({
    url: '/emp/job/save',
    method: 'post',
    data: job
  })
}

// 员工认领工单
// modeId 可以不传：员工档案里有默认工作方式时后端直接用员工的
export function claimWorkOrder(workOrderId, employeeId, quantity, modeId) {
  return request({
    url: '/emp/job/claim',
    method: 'post',
    data: { workOrderId, employeeId, quantity, modeId }
  })
}

// 修改工作记录
export function updateJobInfo(job) {
  return request({
    url: '/emp/job/update',
    method: 'put',
    data: job
  })
}

// 删除工作记录
export function deleteJobInfoById(id) {
  return request({
    url: '/emp/job/delete/' + id,
    method: 'delete'
  })
}

// 按员工ID和日期区间查询员工的工作信息列表
export function queryJobListByEmployeeIdAndDate(
  employeeId,
  startDate,
  endDate,
  factoryId,
  number,
  categoryId,
  currentPage,
  pageSize
) {
  return request({
    url: '/emp/job/list/' + employeeId,
    method: 'get',
    params: {
      ...normalizeDateRangeParams(startDate, endDate),
      factoryId,
      number,
      categoryId,
      currentPage,
      pageSize
    }
  })
}

// 查询工作类型列表
export function queryCategoryList() {
  return request({
    url: '/emp/category/list',
    method: 'get'
  })
}

// 查询薪资
export function querySalaryByCondition(employeeId, startDate, endDate) {
  return request({
    url: '/emp/job/salary/' + employeeId,
    method: 'get',
    params: normalizeDateRangeParams(startDate, endDate)
  })
}
