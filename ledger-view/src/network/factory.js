import { request } from '../base/request.js'

// 查询工厂列表
export function queryFactoryList() {
  return request({
    url: '/emp/factory/list',
    method: 'get'
  })
}

// 员工端按款式编号查询样板列表（仅正常数据）
export function queryFactoryQuotationSampleList(styleNumber, flag = 0) {
  return request({
    url: '/emp/factoryQuotation/sampleList',
    method: 'get',
    params: {
      styleNumber,
      flag
    }
  })
}
