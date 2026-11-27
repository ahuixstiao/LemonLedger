import { request } from '../base/request.js'

// 查询工厂列表
export function queryFactoryList() {
  return request({
    url: '/emp/factory/list',
    method: 'get'
  })
}

// 员工端样板图片列表：按成衣厂 + 款式编号筛选，只返回有图片的款式
export function queryStyleSampleList(factoryId, styleNumber, currentPage = 1, pageSize = 12) {
  return request({
    url: '/emp/factoryStyle/sampleList',
    method: 'get',
    params: {
      factoryId,
      styleNumber,
      currentPage,
      pageSize
    }
  })
}

// 员工端按款式编号查询样板列表（仅正常数据）
export function queryFactoryPieceRateSampleList(styleNumber, flag = 0) {
  return request({
    url: '/emp/factoryPieceRate/sampleList',
    method: 'get',
    params: {
      styleNumber,
      flag
    }
  })
}
