package com.ledger.api.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.common.result.Result;
import com.ledger.db.entity.Factory;
import com.ledger.db.service.IFactoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: ahui
 * @Description: TODO 工厂控制器
 * @DateTime: 2025/10/11 - 10:07
 **/
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/factory")
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
    @GetMapping("/list/{factoryName}")
    public Result<Object> queryFactoryList(
            @PathVariable(required = false) String factoryName,
            @RequestParam(required = false, defaultValue = "1") Integer currentPage,
            @RequestParam(required = false, defaultValue = "5") Integer pageSize,
            @RequestParam(required = false, defaultValue = "0") Integer flag) {

        Page<Factory> page = new Page<>(currentPage, pageSize);

        return Result.ok(
                factoryService.lambdaQuery()
                        .eq(StrUtil.isNotBlank(factoryName), Factory::getFactoryName, factoryName)
                        .eq(Factory::getFlag, flag)
                        .page(page)
        );
    }

}
