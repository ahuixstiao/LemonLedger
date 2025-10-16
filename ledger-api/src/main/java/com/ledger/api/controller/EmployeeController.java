package com.ledger.api.controller;

import cn.hutool.core.lang.intern.InternUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.common.result.Result;
import com.ledger.db.entity.Employee;
import com.ledger.db.service.IEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author: ahui
 * @Description: TODO 员工控制器
 * @DateTime: 2025/10/2 - 20:20
 **/
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/employee")
public class EmployeeController {

    private final IEmployeeService employeeService;


    /**
     * 查询员工列表
     *
     * @param flag 删除状态 0否 1是
     * @return result
     */
    @GetMapping("/list")
    public Result<Object> queryEmployees(@RequestParam(required = false, defaultValue = "0") Integer flag) {

        return employeeService.queryEmployeeList(flag);
    }

    /**
     * 按条件查询员工列表
     *
     * @param name 员工名称
     * @param currentPage 当前页
     * @param pageSize 页面条数
     * @param flag 删除状态 0否 1是
     * @return result
     */
    @GetMapping("/{name}")
    public Result<Object> queryEmployeeInfo(
            @PathVariable @NotNull String name,
            @RequestParam(required = false, defaultValue = "1") Integer currentPage,
            @RequestParam(required = false, defaultValue = "5") Integer pageSize,
            @RequestParam(required = false, defaultValue = "0") Integer flag) {

        return employeeService.queryEmployeeList(name, currentPage, pageSize, flag);
    }

    /**
     * 保存员工信息
     *
     * @param employee 员工实体
     * @return result
     */
    @PostMapping("/save")
    public Result<Object> saveEmployee(Employee employee) {

        return employeeService.saveEmployee(employee);
    }


}
