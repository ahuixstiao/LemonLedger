package com.ledger.api.controller.admin.rank;

import com.ledger.common.result.Result;
import com.ledger.db.service.rank.IAdminRankService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 员工产能排行。
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/admin/rank")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AdminRankController {

    private final IAdminRankService adminRankService;

    /**
     * 区间内按员工汇总产能，按件数倒序。
     *
     * <p>不传日期时按最近 30 天；指定成衣厂时只统计这家厂的活。</p>
     *
     * @param factoryId 成衣厂ID，可为空
     * @param startDate 起始日期 yyyy-MM-dd，可为空
     * @param endDate   结束日期 yyyy-MM-dd，可为空
     * @return result
     */
    @GetMapping("/employees")
    public Result<Object> queryEmployeeRank(
            @RequestParam(required = false) String factoryId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return adminRankService.queryEmployeeRank(parseIntegerParam(factoryId), startDate, endDate);
    }

    /**
     * 将请求参数解析为 Integer，解析失败或为空时返回 null。
     */
    private Integer parseIntegerParam(String value) {
        if (value == null || value.isBlank() || "0".equals(value.trim())) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
