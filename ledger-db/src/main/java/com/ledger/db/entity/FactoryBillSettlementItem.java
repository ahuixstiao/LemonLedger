package com.ledger.db.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ledger.common.entity.ParentEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 结算单明细：导出那一刻这一床的床号、款式、数量与金额。
 *
 * @author ahui
 * @since 2026-11-22
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("factory_bill_settlement_item")
public class FactoryBillSettlementItem extends ParentEntity {

    @Serial
    private static final long serialVersionUID = -2345844203731541287L;

    private Long settlementId;

    private Long billId;

    private String number;

    private String styleNumber;

    private String category;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal amount;
}
