package com.ledger.db.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ledger.common.entity.ParentEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 成衣厂账单结算单：每次导出账单生成一条快照，结清状态记在这条上。
 *
 * @author ahui
 * @since 2026-11-22
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("factory_bill_settlement")
public class FactoryBillSettlement extends ParentEntity {

    @Serial
    private static final long serialVersionUID = 6384251558214531607L;

    /** 未结清 */
    public static final int STATUS_UNPAID = 0;
    /** 部分结清 */
    public static final int STATUS_PARTIAL = 1;
    /** 已结清 */
    public static final int STATUS_PAID = 2;

    private Integer factoryId;

    private String factoryName;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate endDate;

    /** 本单账单笔数 */
    private Integer billCount;

    /** 本单总件数 */
    private Integer totalQuantity;

    /** 本单总金额 */
    private BigDecimal totalAmount;

    /** 已结金额 */
    private BigDecimal paidAmount;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate paidDate;

    /** 0 未结清，1 部分结清，2 已结清 */
    private Integer status;
}
