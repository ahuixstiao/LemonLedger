package com.ledger.db.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 看板用的按天汇总行：账单件数/金额与工资在服务层按日期合并。
 *
 * @author ahui
 */
@Data
public class DashboardDailyDTO {

    /** 日期 yyyy-MM-dd */
    private String date;

    /** 当天账单件数 */
    private BigDecimal billQty;

    /** 当天账单金额 */
    private BigDecimal billAmount;

    /** 当天工资支出 */
    private BigDecimal salary;
}
