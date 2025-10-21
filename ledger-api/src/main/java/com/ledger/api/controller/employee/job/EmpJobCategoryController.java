package com.ledger.api.controller.employee.job;

import com.ledger.common.result.Result;
import com.ledger.db.entity.JobCategory;
import com.ledger.db.service.job.IJobCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: ahui
 * @Description: TODO 工作类别控制器
 * @DateTime: 2025/10/11 - 10:07
 **/
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/emp/category")
public class EmpJobCategoryController {

    private final IJobCategoryService jobCategoryService;

    /**
     * 查询工作类型列表
     *
     * @return result
     */
    @GetMapping("/list")
    public Result<Object> queryJobCategoryList() {

        return Result.ok(jobCategoryService.lambdaQuery().eq(JobCategory::getFlag, 0).list());
    }
}
