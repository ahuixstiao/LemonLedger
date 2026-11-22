package com.ledger.db.service.settlement;

import com.ledger.common.result.Result;
import com.ledger.db.entity.FactoryBillSettlement;
import com.ledger.db.entity.FactoryBillSettlementItem;
import com.ledger.db.entity.dto.FactoryBillDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 成衣厂账单结算单：导出快照与结清记录。
 *
 * @author ahui
 * @since 2026-11-22
 */
public interface IFactoryBillSettlementService {

    /**
     * 导出账单时落一条快照。
     *
     * <p>同一成衣厂、同一区间如果还有没结清的结算单，就把金额和明细刷新到那条上；
     * 已经结清的不动，另开一条，避免把结算凭证改掉。</p>
     *
     * @param factoryId   成衣厂ID
     * @param factoryName 成衣厂名称
     * @param startDate   导出起始日期，可为空
     * @param endDate     导出截止日期，可为空
     * @param rows        本次导出的账单行（不含总计行）
     * @return 结算单
     */
    FactoryBillSettlement snapshotOnExport(Integer factoryId,
                                           String factoryName,
                                           LocalDate startDate,
                                           LocalDate endDate,
                                           List<FactoryBillDto> rows);

    /**
     * 结算单分页列表。
     *
     * @param factoryId  成衣厂ID，可为空
     * @param status     状态 0/1/2，可为空
     * @param startDate  导出日期下界，可为空
     * @param endDate    导出日期上界，可为空
     * @param currentPage 页码
     * @param pageSize    每页条数
     * @return result
     */
    Result<Object> querySettlementList(Integer factoryId, Integer status, String startDate, String endDate,
                                       Integer currentPage, Integer pageSize);

    /**
     * 结算单明细。
     *
     * @param id 结算单ID
     * @return result
     */
    Result<Object> querySettlementItems(Integer id);

    /**
     * 登记结清：记已付金额与付款日期，状态按金额自动算。
     *
     * @param id        结算单ID
     * @param paidAmount 已付金额
     * @param paidDate   付款日期，可为空
     * @param remark     备注，可为空
     * @return result
     */
    Result<Object> settle(Integer id, BigDecimal paidAmount, LocalDate paidDate, String remark);

    /**
     * 作废结算单（逻辑删除）。
     *
     * @param id 结算单ID
     * @return result
     */
    Result<Object> voidSettlement(Integer id);

    /**
     * 取结算单（用于按快照重新导出）。
     *
     * @param id 结算单ID
     * @return 结算单，不存在时返回 null
     */
    FactoryBillSettlement getSettlement(Integer id);

    /**
     * 取结算单明细（用于按快照重新导出）。
     *
     * @param id 结算单ID
     * @return 明细列表
     */
    List<FactoryBillSettlementItem> getSettlementItems(Integer id);
}
