package com.ledger.api.controller.admin.job;

import com.ledger.common.dto.admin.JobCategoryQueryDTO;
import com.ledger.common.result.Result;
import com.ledger.db.entity.JobCategory;
import com.ledger.db.service.job.IJobCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端工作类型控制器。
 */
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/admin/jobCategory")
public class AdminJobCategoryController {

    private final IJobCategoryService jobCategoryService;

    /**
     * 按条件分页查询工作类型列表。
     */
    @GetMapping
    public Result<Object> queryJobCategoryList(JobCategoryQueryDTO queryDTO) {
        return jobCategoryService.queryCategoryList(
                queryDTO.getCategory(),
                queryDTO.getCurrentPage(),
                queryDTO.getPageSize(),
                queryDTO.getFlag()
        );
    }

    /**
     * 新增工作类型。
     */
    @PostMapping
    public Result<Object> saveJobCategory(@RequestBody JobCategory jobCategory) {
        return jobCategoryService.saveJobCategoryInfo(jobCategory);
    }

    /**
     * 删除工作类型。
     */
    @DeleteMapping("/{id}")
    public Result<Object> deleteJobCategory(@PathVariable Integer id) {
        return jobCategoryService.removeJobCategoryInfo(id);
    }
}
