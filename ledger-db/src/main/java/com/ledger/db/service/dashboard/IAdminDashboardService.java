package com.ledger.db.service.dashboard;

import com.ledger.common.result.Result;

/**
 * 后台首页看板：账单、工资、毛利与对账情况的总览。
 *
 * @author ahui
 */
public interface IAdminDashboardService {

    /**
     * 汇总区间内的经营数据。
     *
     * @param startDate 起始日期 yyyy-MM-dd，可为空（默认近 7 天）
     * @param endDate   结束日期 yyyy-MM-dd，可为空（默认今天）
     * @return result
     */
    Result<Object> querySummary(String startDate, String endDate);
}
