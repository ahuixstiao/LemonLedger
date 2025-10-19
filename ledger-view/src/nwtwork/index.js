import { request } from './request.js'

export function queryEmployees(data) {
  return request({
    url: '/employee/list',
    method: 'get',
    params: { data }
  })
}

// 员工注册
export function saveEmployee(employee) {
  return request({
    url: '/employee/save',
    method: 'post',
    data: employee
  })
} 

// 添加工作记录
export function saveJobInfo(job) {
  return request({
    url: '/job/save',
    method: 'post',
    data: job
  })
} 

// 按员工ID和日期区间查询员工的工作信息列表
export function queryJobListByEmployeeIdAndDate(employeeId,startDate,endDate) {
  return request({
    url: '/job/list/'+ employeeId,
    method: 'get',
    params: {startDate,endDate}
  })
}

// 查询工厂列表
export function queryFactoryList() {
  return request({
    url: '/factory/list',
    method: 'get',
  })
}

// 查询工作类型列表
export function queryCategoryList() {
    return request({
        url: '/category/list',
        method: 'get',
    })
}

// 查询工作方式列表
export function queryModeList() {
    return request({
        url: '/mode/list',
        method: 'get'
    })
}

// 查询薪资
export function querySalaryByCondition(employeeId,startDate,endDate) {
  return request({
    url: '/job/salary/'+ employeeId,
    method: 'get',
       params: {startDate,endDate}
  })
}
