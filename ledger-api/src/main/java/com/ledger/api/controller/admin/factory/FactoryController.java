package com.ledger.api.controller.admin.factory;

import com.ledger.common.result.Result;
import com.ledger.db.entity.Factory;
import com.ledger.db.service.factory.IFactoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: ahui
 * @Description: TODO 成衣厂控制器
 * @DateTime: 2025/10/11 - 10:07
 **/
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/admin/factory")
public class FactoryController {

    private final IFactoryService factoryService;

    /**
     * 查询工厂列表
     *
     * @param factoryName 工厂名称
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @param flag        状态 0-启用 1-禁用
     * @return result
     */
    @GetMapping("/list")
    public Result<Object> queryFactoryList(
            @RequestParam(required = false) String factoryName,
            @RequestParam(required = false, defaultValue = "1") Integer currentPage,
            @RequestParam(required = false, defaultValue = "5") Integer pageSize,
            @RequestParam(required = false, defaultValue = "0") Integer flag) {

        return factoryService.queryFactoryList(factoryName, currentPage, pageSize, flag);
    }

    /**
     * 保存工厂信息
     * @param factory 工厂实体
     * @return result
     */
    @PostMapping("/save")
    public Result<Object> saveFactory(Factory factory) {

        return factoryService.saveFactoryInfo(factory);
    }

}
