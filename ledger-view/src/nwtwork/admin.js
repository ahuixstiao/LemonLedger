import {request} from './request.js'

// 按条件查询成衣厂账单列表
export function queryFactoryBillList(
    factoryId,
    number,
    styleNumber,
    categoryId,
    flag,
    startDate,
    endDate,
    currentPage,
    pageSize
) {
    return request({
        url: '/admin/factoryBill/list',
        method: 'get',
        params: {
            factoryId,
            number,
            styleNumber,
            categoryId,
            flag,
            startDate,
            endDate,
            currentPage,
            pageSize
        }
    })
}

// 保存成衣厂账单
export function saveFactoryBillInfo(bill) {
    return request({
        url: '/admin/factoryBill/save',
        method: 'post',
        data: bill
    })
}

// 统计指定成衣厂账单
export function statisticalFactoryBill(factoryId, startDate, endDate) {
    return request({
        url: '/admin/factoryBill/bill/' + factoryId,
        method: 'get',
        params: {
            startDate,
            endDate
        }
    })
}