package com.ledger.api.controller.admin.factory;

import com.ledger.common.dto.admin.FactoryQueryDTO;
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
    @GetMapping
    public Result<Object> queryFactoryList(FactoryQueryDTO queryDTO) {
        return factoryService.queryFactoryList(
                queryDTO.getFactoryName(),
                queryDTO.getCurrentPage(),
                queryDTO.getPageSize(),
                queryDTO.getFlag()
        );
    }

    /**
     * 保存工厂信息
     * @param factory 工厂实体
     * @return result
     */
    @PostMapping
    public Result<Object> saveFactory(@RequestBody Factory factory) {
        return factoryService.saveFactoryInfo(factory);
    }

}
