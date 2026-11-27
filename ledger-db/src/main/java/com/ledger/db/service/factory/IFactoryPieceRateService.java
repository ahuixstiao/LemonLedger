package com.ledger.db.service.factory;

import com.ledger.common.result.Result;
import com.ledger.db.entity.FactoryPieceRate;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 成衣厂计件单价表 服务类
 * </p>
 *
 * @author ahui
 * @since 2025-10-16
 */
public interface IFactoryPieceRateService extends IService<FactoryPieceRate> {

    /**
     * 查询成衣厂计件单价列表 如果有条件则按条件查询
     *
     * @param factoryId   工厂ID
     * @param styleNumber 款式编号
     * @param categoryId  工作类型ID
     * @param currentPage 当前页
     * @param pageSize    页面条数
     * @param flag        状态
     * @return result
     */
    Result<Object> queryFactoryPieceRateListByCondition(
            Integer factoryId, String styleNumber, Integer categoryId,
            Integer currentPage, Integer pageSize, Integer flag);

    /**
     * 账单用：查询某成衣厂的计件单价下拉项（款式编号 / 工作类型 / 单价）
     *
     * @param factoryId 成衣厂ID
     * @param flag      删除状态 0否 1是
     * @return result
     */
    Result<Object> queryPieceRateOptionsByFactoryId(Integer factoryId, Integer flag);

    /**
     * 保存成衣厂计件单价信息
     *
     * @param factoryPieceRate 成衣厂计件单价实体
     * @return result
     */
    Result<Object> saveFactoryPieceRateInfo(FactoryPieceRate factoryPieceRate);

    /**
     * 更新成衣厂计件单价信息；单价有变化时按新单价重算指定日期区间内的账单金额
     *
     * @param factoryPieceRate 成衣厂计件单价实体
     * @param startDate        受影响账单的开始日期（改单价时必填）
     * @param endDate          受影响账单的结束日期（改单价时必填）
     * @return result，data 里带同步的账单条数
     */
    Result<Object> updateFactoryPieceRateInfo(FactoryPieceRate factoryPieceRate, String startDate, String endDate);

    /**
     * 删除成衣厂计件单价信息
     *
     * @param id 成衣厂计件单价ID
     * @return result
     */
    Result<Object> deleteFactoryPieceRateById(Integer id);

}
