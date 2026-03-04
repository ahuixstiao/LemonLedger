package com.ledger.api.controller.admin.job;

import com.ledger.common.dto.admin.JobQuotationQueryDTO;
import com.ledger.common.result.Result;
import com.ledger.db.entity.JobQuotation;
import com.ledger.db.service.job.IJobQuotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端工作报价控制器。
 */
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/admin/jobQuotation")
public class AdminJobQuotationController {

    private final IJobQuotationService jobQuotationService;

    /**
     * 按条件分页查询工作报价列表。
     */
    @GetMapping
    public Result<Object> queryJobQuotationList(JobQuotationQueryDTO queryDTO) {
        return jobQuotationService.queryJobQuotationListByCondition(
                queryDTO.getModeId(),
                queryDTO.getCategoryId(),
                queryDTO.getCurrentPage(),
                queryDTO.getPageSize(),
                queryDTO.getFlag()
        );
    }

    /**
     * 新增工作报价。
     */
    @PostMapping
    public Result<Object> saveJobQuotation(@RequestBody JobQuotation jobQuotation) {
        return jobQuotationService.saveJobQuotationInfo(jobQuotation);
    }

    /**
     * 更新工作报价。
     */
    @PutMapping("/{id}")
    public Result<Object> updateJobQuotation(@PathVariable Integer id, @RequestBody JobQuotation jobQuotation) {
        jobQuotation.setId(id);
        return jobQuotationService.updateJobQuotationInfo(jobQuotation);
    }

    /**
     * 删除工作报价。
     */
    @DeleteMapping("/{id}")
    public Result<Object> deleteJobQuotation(@PathVariable Integer id) {
        return jobQuotationService.removeJobQuotationInfo(id);
    }
}
