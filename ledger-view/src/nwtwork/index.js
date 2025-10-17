import { request } from './request.js'

export function queryEmployees(data) {
  return request({
    url: '/employee/list',
    method: 'get',
    params: { data }
  })
}

export function saveJobInfo(employee) {
  
  return request({
    url: '/employee/save',
    method: 'post',
    data: employee
  })
} 

export function queryJobListByIDAndDate(employeeId,startDate,endDate) {
  return request({
    url: '/job/list/'+ employeeId,
    method: 'get',
    params: {startDate,endDate}
  })
}

export function queryFactoryList() {
  return request({
    url: '/factory/list',
    method: 'get',
  })
}