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
     * @return result
     */
    Result<Object> queryFactoryBillListByCondition(Integer factoryId, Integer number, Integer styleNumber, Integer categoryId, Integer flag, String startDate, String endDate);


}
