package com.ledger.api.controller.admin.dashboard;

import com.ledger.common.result.Result;
import com.ledger.db.service.dashboard.IAdminDashboardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台首页看板：一张接口把区间内的账、钱、对账情况一次给前端。
 *
 * @author ahui
 */
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/admin/dashboard")
@Validated
@Slf4j
public class AdminDashboardController {

    private final IAdminDashboardService adminDashboardService;

    /**
     * 看板汇总。
     *
     * @param startDate 起始日期 yyyy-MM-dd，可为空（默认近 7 天）
     * @param endDate   结束日期 yyyy-MM-dd，可为空（默认今天）
     * @return result
     */
    @GetMapping("/summary")
    public Result<Object> querySummary(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return adminDashboardService.querySummary(startDate, endDate);
    }
}
