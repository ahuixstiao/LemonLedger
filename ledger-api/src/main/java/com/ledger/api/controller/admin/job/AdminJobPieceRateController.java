package com.ledger.api.controller.admin.job;

import com.ledger.common.dto.admin.JobPieceRateQueryDTO;
import com.ledger.common.result.Result;
import com.ledger.db.entity.JobPieceRate;
import com.ledger.db.service.job.IJobPieceRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端员工计件单价控制器。
 */
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/admin/jobPieceRate")
public class AdminJobPieceRateController {

    private final IJobPieceRateService jobPieceRateService;

    /**
     * 按条件分页查询员工计件单价列表。
     */
    @GetMapping
    public Result<Object> queryJobPieceRateList(JobPieceRateQueryDTO queryDTO) {
        return jobPieceRateService.queryJobPieceRateListByCondition(
                queryDTO.getModeId(),
                queryDTO.getCategoryId(),
                queryDTO.getCurrentPage(),
                queryDTO.getPageSize(),
                queryDTO.getFlag()
        );
    }

    /**
     * 新增员工计件单价。
     */
    @PostMapping
    public Result<Object> saveJobPieceRate(@RequestBody JobPieceRate jobPieceRate) {
        return jobPieceRateService.saveJobPieceRateInfo(jobPieceRate);
    }

    /**
     * 更新员工计件单价。
     */
    @PutMapping("/{id}")
    public Result<Object> updateJobPieceRate(@PathVariable Integer id, @RequestBody JobPieceRate jobPieceRate) {
        jobPieceRate.setId(id);
        return jobPieceRateService.updateJobPieceRateInfo(jobPieceRate);
    }

    /**
     * 删除员工计件单价。
     */
    @DeleteMapping("/{id}")
    public Result<Object> deleteJobPieceRate(@PathVariable Integer id) {
        return jobPieceRateService.removeJobPieceRateInfo(id);
    }
}
