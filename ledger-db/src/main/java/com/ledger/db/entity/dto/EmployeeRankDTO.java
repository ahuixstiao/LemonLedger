package com.ledger.db.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 员工产能排行的一行。
 *
 * <p>区间内按员工汇总：做了多少件、拿了多少工资、出了几天勤，
 * 以及压花/刮胶各占多少，用来看谁做得多、谁做的活更值钱。</p>
 */
@Data
public class EmployeeRankDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer employeeId;

    private String employeeName;

    /**
     * 区间内件数合计
     */
    private Integer quantity;

    /**
     * 区间内工资合计
     */
    private BigDecimal salary;

    /**
     * 出勤天数（有记录的日期数）
     */
    private Integer days;

    /**
     * 压花件数
     */
    private Integer embossQty;

    /**
     * 刮胶件数
     */
    private Integer glueQty;

    /**
     * 平均单价（元/件）= 工资 / 件数，件数为 0 时为空
     */
    private BigDecimal avgPrice;
}
