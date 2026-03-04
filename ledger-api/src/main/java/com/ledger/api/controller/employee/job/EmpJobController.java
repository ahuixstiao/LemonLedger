package com.ledger.api.controller.employee.job;

import com.ledger.common.dto.employee.WorkOrderClaimDTO;
import com.ledger.common.result.Result;
import com.ledger.db.entity.Job;
import com.ledger.db.service.job.IJobService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static org.springframework.util.StringUtils.hasText;

import com.sun.istack.NotNull;

/**
 * @Author: ahui
 * @Description: TODO 员工工作内容控制器
 * @DateTime: 2025/10/10 - 14:47
 **/
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/emp/job")
public class EmpJobController {

    private final IJobService jobService;

    /**
     * 默认查询当天所有员工的工作信息
     * 如果传入员工ID 则按员工ID 查询当天 或 由员工指定的起止日期的工作信息
     *
     * @param employeeId  员工id
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @param factoryId   指定工厂
     * @param number      指定床号
     * @param categoryId  指定工作类型
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @return result
     */
    // localhost:8080/job/list/1?startDate=2025-10-01&endDate=2025-10-06
    @GetMapping("/list/{employeeId}")
    public Result<Object> queryJobListByIDAndDate(
            @PathVariable @NotNull Integer employeeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String factoryId,
            @RequestParam(required = false) String number,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false, defaultValue = "1") String currentPage,
            @RequestParam(required = false, defaultValue = "5") String pageSize,
            @RequestParam(required = false, defaultValue = "0") String flag
    ) {
        Integer parsedFactoryId = parseIntegerParam(factoryId);
        Integer parsedCategoryId = parseIntegerParam(categoryId);
        Integer parsedCurrentPage = parseIntegerParam(currentPage, 1);
        Integer parsedPageSize = parseIntegerParam(pageSize, 5);
        Integer parsedFlag = parseIntegerParam(flag, 0);

        return jobService.queryJobListByEmployeeIDAndDate(
                employeeId, startDate, endDate,
                parsedFactoryId, number, parsedCategoryId,
                parsedCurrentPage, parsedPageSize, parsedFlag);
    }

    /**
     * 统计指定员工某段时间内的工资信息
     *
     * @param employeeId 员工ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @param flag       删除状态 0 正常 1 删除
     * @return result
     */
    @GetMapping("/salary/{employeeId}")
    public Result<Object> statisticalSalary(
            @PathVariable @NotNull Integer employeeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "0") String flag) {

        Integer parsedFlag = parseIntegerParam(flag, 0);
        return jobService.statisticalSalary(employeeId, startDate, endDate, parsedFlag);
    }

    /**
     * 保存工作信息
     *
     * @param job 工作信息实体
     * @return result
     */
    @PostMapping("/save")
    public Result<Object> saveJobInfo(@RequestBody Job job) {
        return jobService.saveJobInfo(job);
    }

    /**
     * 员工认领工单并写入工作信息。
     *
     * @param claimDTO 认领参数
     * @return result
     */
    @PostMapping("/claim")
    public Result<Object> claimWorkOrder(@RequestBody WorkOrderClaimDTO claimDTO) {
        return jobService.claimWorkOrder(claimDTO);
    }

    /**
     * 修改工作信息
     *
     * @param job 新的工作信息
     * @return result
     */
    @PutMapping("/update")
    public Result<Object> updateJobInfo(@RequestBody Job job) {
        return jobService.updateJobInfo(job);
    }

    /**
     * 删除工作信息
     *
     * @param jobId 工作ID
     * @return result
     */
    @DeleteMapping("/delete/{jobId}")
    public Result<Object> deleteJobInfo(@PathVariable @NotNull Integer jobId) {

        return jobService.deleteJobInfo(jobId);
    }

    private Integer parseIntegerParam(String value) {
        return parseIntegerParam(value, null);
    }

    private Integer parseIntegerParam(String value, Integer defaultValue) {
        if (!hasText(value)) {
            return defaultValue;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

}
