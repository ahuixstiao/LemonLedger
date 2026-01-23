package com.ledger.api.controller.admin.factory;

import com.ledger.common.result.Result;
import com.ledger.db.service.factory.IFactoryJobCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 工作类型表 前端控制器
 * </p>
 *
 * @author ahui
 * @since 2026-01-23
 */
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
@RequestMapping("/admin/factoryJobCategory")
public class FactoryJobCategoryController {

    private final IFactoryJobCategoryService factoryJobCategoryService;

    @GetMapping("/list")
    public Result<Object> queryFactoryJobCategoryList() {

        return factoryJobCategoryService.queryFactoryJobCategoryList();
    }

}
