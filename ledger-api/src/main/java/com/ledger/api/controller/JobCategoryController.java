package com.ledger.api.controller;

import com.ledger.common.result.Result;
import com.ledger.db.service.IJobCategoryService;
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
@RequestMapping("/category")
public class JobCategoryController {

    private final IJobCategoryService jobCategoryService;

    // localhost:8080/list/xxx?currentPage=1&pageSize=5&flag=0
    // required属性 表示该参数是否为必须传递
    @GetMapping("/list/{category}")
    public Result<Object> queryCategoryList(
            @PathVariable(required = false) String category,
            @RequestParam(required = false, defaultValue = "1") Integer currentPage,
            @RequestParam(required = false, defaultValue = "5") Integer pageSize,
            @RequestParam(required = false, defaultValue = "0") Integer flag
    ) {


        return jobCategoryService.queryCategoryList(category, currentPage, pageSize, flag);
    }

}
