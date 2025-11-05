package com.ledger.db.service.factory;

import com.ledger.common.result.Result;
import com.ledger.db.entity.FactoryQuotation;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 成衣厂报价表 服务类
 * </p>
 *
 * @author ahui
 * @since 2025-10-16
 */
public interface IFactoryQuotationService extends IService<FactoryQuotation> {

    /**
     * 查询成衣厂报价列表 如果有条件则按条件查询
     *
     * @param factoryId 工厂ID
     * @param categoryId 工作类型ID
     * @param currentPage 当前页
     * @param pageSize    页面条数
     * @return result
     */
    Result<Object> queryFactoryQuotationListByCondition(
            Integer factoryId, String styleNumber, Integer categoryId,
            Integer currentPage, Integer pageSize, Integer flag);

}
