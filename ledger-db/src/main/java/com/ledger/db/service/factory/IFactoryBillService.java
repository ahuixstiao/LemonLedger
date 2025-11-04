package com.ledger.db.service.factory;

import com.ledger.common.result.Result;
import com.ledger.db.entity.FactoryBill;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 成衣厂账单表 服务类
 * </p>
 *
 * @author ahui
 * @since 2025-10-15
 */
public interface IFactoryBillService extends IService<FactoryBill> {

    /**
     * 按条件查询成衣厂账单列表  如果不传递参数则默认查询全部成衣厂账单列表
     *
     * @param factoryId   工厂ID
     * @param number      床号
     * @param styleNumber 款式编号
     * @param categoryId  工作类型
     * @param flag        删除状态 0否 1是
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @param currentPage 当前页
     * @param pageSize    页面条数
     * @return result
     */
    Result<Object> queryFactoryBillListByCondition(
            Integer factoryId, String number,
            String styleNumber, Integer categoryId, Integer flag,
            String startDate, String endDate,
            Integer currentPage, Integer pageSize);

    /**
     * 统计成衣厂账单
     *
     * @param factoryId 成衣厂ID
     * @param startDate 起止日期
     * @param endDate   起止日期
     * @param flag      状态
     * @return result
     */
    Result<Object> statisticalBill(Integer factoryId, String startDate, String endDate, Integer flag);

    /**
     * 保存账单信息
     *
     * @param bill 账单实体
     * @return result
     */
    Result<Object> saveFactoryBillInfo(FactoryBill bill);

    /**
     * 修改账单信息
     *
     * @param bill 账单信息实体
     * @return result
     */
    Result<Object> updateFactoryBillInfo(FactoryBill bill);

    /**
     * 删除账单信息
     *
     * @param factoryBillId 账单ID
     * @return result
     */
    Result<Object> deleteFactoryBillInfo(Integer factoryBillId);
}
