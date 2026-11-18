package com.ledger.db.entity.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 账单与认领的对账行（一张床一行）。
 *
 * <p>成衣厂报的账单数量，和员工实际认领的数量，本来就是两套账：
 * 厂里说这床做了多少、员工说这床做了多少，中间会有差额。
 * 这个 DTO 把两边放在一行里，差额 = 账单数量 - 实际完成数量。</p>
 *
 * @author ahui
 * @since 2026-11-18
 */
@Data
public class ReconcileRowDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 床ID
     */
    private Integer batchId;

    /**
     * 成衣厂名称
     */
    private String factoryName;

    /**
     * 床号
     */
    private String number;

    /**
     * 款式编号
     */
    private String styleNumber;

    /**
     * 账单数量合计（区间内）
     */
    private Integer billQty;

    /**
     * 账单金额合计（区间内）
     */
    private BigDecimal billAmount;

    /**
     * 压花认领数量（区间内）
     */
    private Integer claimedEmboss;

    /**
     * 刮胶认领数量（区间内）
     */
    private Integer claimedGlue;

    /**
     * 实际完成数量：两道工序都做过才算完成，取两者较小值
     */
    private Integer doneQty;

    /**
     * 工资合计（区间内）
     */
    private BigDecimal salaryTotal;

    /**
     * 差额 = 账单数量 - 实际完成数量；正数表示厂里报的量还没做完，负数表示做超了
     */
    private Integer diffQty;
}
