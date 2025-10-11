package com.ledger.api.controller;

import com.ledger.common.result.Result;
import com.ledger.db.service.IJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.time.format.DateTimeFormatter;

/**
 * @Author: ahui
 * @Description: TODO 工作内容控制器
 * @DateTime: 2025/10/10 - 14:47
 **/
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/job")
public class JobController {

    private final IJobService jobService;

    /**
     * 默认查询当天所有员工的工作信息
     *
     * @return result
     */
    // localhost:8080/job/list
    @GetMapping("/list")
    public Result<Object> queryJobListByDefaultThatDay(
            @RequestParam(required = false, defaultValue = "1") Integer currentPage,
            @RequestParam(required = false, defaultValue = "5") Integer pageSize) {

        return jobService.queryJobListByDefaultCurrentDay(currentPage, pageSize);
    }

    /**
     * 查询指定员工当天或某天的工作信息
     *
     * @param employeeId  员工id
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @return result
     */
    // localhost:8080/job/list/1?startDate=2025-10-01&endDate=2025-10-06
    @GetMapping("/list/employee/{employeeId}")
    public Result<Object> queryJobListByIDAndDate(
            @PathVariable @NotNull Integer employeeId,
            @RequestParam(required = false, defaultValue = "1") Integer currentPage,
            @RequestParam(required = false, defaultValue = "5") Integer pageSize,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {


        return jobService.queryJobListByEmployeeIDAndDate(employeeId, startDate, endDate, currentPage, pageSize);
    }

}
