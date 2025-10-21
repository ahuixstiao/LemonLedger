package com.ledger.api.controller.employee.factory;

import com.ledger.common.result.Result;
import com.ledger.db.entity.Factory;
import com.ledger.db.service.factory.IFactoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: ahui
 * @Description: TODO
 * @DateTime: 2025/10/21 - 20:34
 **/
@RestController
@RequiredArgsConstructor(onConstructor_ =  @Autowired)
@RequestMapping("/emp/factory")
public class EmpFactoryController {

    private final IFactoryService factoryService;

    /**
     * 查询工厂列表
     *
     * @return result
     */
    @GetMapping("/list")
    public Result<Object> queryFactoryList() {

        return Result.ok(factoryService.lambdaQuery().eq(Factory::getFlag, 0).list());
    }

}
