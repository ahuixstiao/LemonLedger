package com.ledger.api.controller.employee.job;

import com.ledger.common.result.Result;
import com.ledger.db.entity.JobMode;
import com.ledger.db.service.job.IJobModeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: ahui
 * @Description: TODO 工作方式控制器
 * @DateTime: 2025/10/11 - 10:07
 **/
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/emp/mode")
public class EmpJobModeController {

    private final IJobModeService jobModeService;

    /**
     * 查询工作方式集合
     *
     * @return reuslt
     */
    @GetMapping("/list")
    public Result<Object> queryJobModeList() {

        return Result.ok(jobModeService.lambdaQuery().eq(JobMode::getFlag, 0).list());
    }
}
