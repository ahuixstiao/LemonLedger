package com.ledger.api.controller.job;

import com.ledger.common.result.Result;
import com.ledger.db.entity.JobMode;
import com.ledger.db.service.job.IJobModeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

/**
 * @Author: ahui
 * @Description: TODO 工作方式控制器
 * @DateTime: 2025/10/11 - 10:07
 **/
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/mode")
public class JobModeController {

    private final IJobModeService jobModeService;

    /**
     * 查询工作方式集合
     *
     * @param mode        工作方式
     * @param currentPage 当前页
     * @param pageSize    页面条数
     * @return reuslt
     */
    @GetMapping("/list/{mode}")
    public Result<Object> queryJobModeList(
            @PathVariable(required = false) String mode,
            @RequestParam(required = false, defaultValue = "1") Integer currentPage,
            @RequestParam(required = false, defaultValue = "5") Integer pageSize) {

        return jobModeService.queryJobModeList(currentPage, pageSize, mode);
    }

    /**
     * 保存工作方式信息
     *
     * @param jobMode 工作方式实体
     * @return result
     */
    @PostMapping("/save")
    public Result<Object> saveJobModeInfo(JobMode jobMode) {

        return jobModeService.saveJobModeInfo(jobMode);
    }

    /**
     * 删除工作方式信息
     *
     * @param jobModeId 工作方式ID
     * @return result
     */
    @DeleteMapping("/remove/{jobModeId}")
    public Result<Object> removeJobModeById(@PathVariable @NotNull Integer jobModeId) {

        return jobModeService.removeJobModeInfo(jobModeId);
    }


}
