package com.ledger.db.entity.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 成衣厂毛利统计（收入 - 工资）。
 *
 * <p>收入 = 该成衣厂账单表 bill 的合计；工资 = 该成衣厂工作记录表 salary 的合计。
 * 两个数字原来各在各的页面里，要看"这一单到底赚没赚钱"得自己拿计算器减。</p>
 *
 * @author ahui
 * @since 2025-11-06
 */
@Data
public class FactoryGrossProfitDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 成衣厂ID
     */
    private Integer factoryId;

    /**
     * 成衣厂名称
     */
    private String factoryName;

    /**
     * 账单收入合计
     */
    private BigDecimal billTotal;

    /**
     * 工资支出合计
     */
    private BigDecimal salaryTotal;

    /**
     * 毛利 = 账单收入 - 工资支出
     */
    private BigDecimal grossProfit;
}
