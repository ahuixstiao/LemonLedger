package com.ledger.db.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 毛利趋势的逐日数据点。
 *
 * <p>账单按 {@code factory_bill.created_date} 汇总，工资按 {@code job.created_date} 汇总，
 * 两边都只算未删除的记录；某天只有一边有数据时另一边补 0。</p>
 */
@Data
public class GrossTrendPointDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日期，yyyy-MM-dd
     */
    private String date;

    /**
     * 当天账单收入
     */
    private BigDecimal billTotal;

    /**
     * 当天工资支出
     */
    private BigDecimal salaryTotal;

    /**
     * 当天毛利 = 账单收入 - 工资支出
     */
    private BigDecimal grossProfit;
}
