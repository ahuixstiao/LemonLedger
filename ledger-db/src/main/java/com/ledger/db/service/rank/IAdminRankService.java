package com.ledger.db.service.rank;

import com.ledger.common.result.Result;

/**
 * 员工产能排行。
 */
public interface IAdminRankService {

    /**
     * 区间内按员工汇总件数、工资与出勤天数。
     *
     * @param factoryId 成衣厂ID，可为空
     * @param startDate 起始日期 yyyy-MM-dd，可为空（默认最近 30 天）
     * @param endDate   结束日期 yyyy-MM-dd，可为空
     * @return result，data 里是 {@code Map}：startDate/endDate/totalQuantity/totalSalary/people/rows
     */
    Result<Object> queryEmployeeRank(Integer employeeId, String startDate, String endDate);
}
