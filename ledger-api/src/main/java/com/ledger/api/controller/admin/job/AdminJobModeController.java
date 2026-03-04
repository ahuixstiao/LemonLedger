package com.ledger.api.controller.admin.job;

import com.ledger.common.dto.admin.JobModeQueryDTO;
import com.ledger.common.result.Result;
import com.ledger.db.entity.JobMode;
import com.ledger.db.service.job.IJobModeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端工作方式控制器。
 */
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/admin/jobMode")
public class AdminJobModeController {

    private final IJobModeService jobModeService;

    /**
     * 按条件分页查询工作方式列表。
     */
    @GetMapping
    public Result<Object> queryJobModeList(JobModeQueryDTO queryDTO) {
        return jobModeService.queryJobModeList(
                queryDTO.getCurrentPage(),
                queryDTO.getPageSize(),
                queryDTO.getMode()
        );
    }

    /**
     * 新增工作方式。
     */
    @PostMapping
    public Result<Object> saveJobMode(@RequestBody JobMode jobMode) {
        return jobModeService.saveJobModeInfo(jobMode);
    }

    /**
     * 删除工作方式。
     */
    @DeleteMapping("/{id}")
    public Result<Object> deleteJobMode(@PathVariable Integer id) {
        return jobModeService.removeJobModeInfo(id);
    }
}
