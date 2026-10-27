package com.ledger.api.controller.admin.factory;

import com.ledger.common.result.Result;
import com.ledger.db.entity.FactoryJobCategory;
import com.ledger.db.service.factory.IFactoryJobCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping
    public Result<Object> saveFactoryJobCategory(@RequestBody FactoryJobCategory factoryJobCategory) {
        return factoryJobCategoryService.saveFactoryJobCategoryInfo(factoryJobCategory);
    }

    @PutMapping
    public Result<Object> updateFactoryJobCategory(@RequestBody FactoryJobCategory factoryJobCategory) {
        return factoryJobCategoryService.updateFactoryJobCategoryInfo(factoryJobCategory);
    }

    @DeleteMapping("/{id}")
    public Result<Object> deleteFactoryJobCategory(@PathVariable Integer id) {
        return factoryJobCategoryService.removeFactoryJobCategoryInfo(id);
    }

}
