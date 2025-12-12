import {request} from './request.js'

// 查询员工列表
export function queryEmployees() {
    return request({
        url: '/emp/list',
        method: 'get'
    })
}

// 员工注册
export function saveEmployee(employee) {
    return request({
        url: '/emp/save',
        method: 'post',
        data: employee
    })
}

// 添加工作记录
export function saveJobInfo(job) {
    return request({
        url: '/emp/job/save',
        method: 'post',
        data: job
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
        params: {startDate, endDate, factoryId, number, categoryId, currentPage, pageSize}
    })
}

// 查询工厂列表
export function queryFactoryList() {
    return request({
        url: '/emp/factory/list',
        method: 'get'
    })
}

// 查询工作类型列表
export function queryCategoryList() {
    return request({
        url: '/emp/category/list',
        method: 'get'
    })
}

// 查询工作方式列表
export function queryModeList() {
    return request({
        url: '/emp/mode/list',
        method: 'get'
    })
}

// 查询薪资
export function querySalaryByCondition(employeeId, startDate, endDate) {
    return request({
        url: '/emp/job/salary/' + employeeId,
        method: 'get',
        params: {startDate, endDate}
    })
}
