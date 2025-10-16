package com.ledger.api.controller.job;

import com.ledger.common.result.Result;
import com.ledger.db.entity.Job;
import com.ledger.db.service.job.IJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

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
            @RequestParam(required = false, defaultValue = "5") Integer pageSize,
            @RequestParam(required = false, defaultValue = "0") Integer flag) {

        return jobService.queryJobListByDefaultCurrentDay(currentPage, pageSize, flag);
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
    @GetMapping("/list/{employeeId}")
    public Result<Object> queryJobListByIDAndDate(
            @PathVariable @NotNull Integer employeeId,
            @RequestParam(required = false, defaultValue = "1") Integer currentPage,
            @RequestParam(required = false, defaultValue = "5") Integer pageSize,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "0") Integer flag
    ) {

        return jobService.queryJobListByEmployeeIDAndDate(employeeId, startDate, endDate, currentPage, pageSize, flag);
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
            @RequestParam(required = false, defaultValue = "0") Integer flag) {

        return jobService.statisticalSalary(employeeId, startDate, endDate, flag);
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
     * 修改工作信息
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

}
