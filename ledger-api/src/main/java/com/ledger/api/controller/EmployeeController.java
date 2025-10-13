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
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @return result
     */
    @GetMapping("/list/{name}")
    public Result<Object> queryEmployees(
            @PathVariable(required = false) String name,
            @RequestParam(required = false, defaultValue = "1") Integer currentPage,
            @RequestParam(required = false, defaultValue = "5") Integer pageSize,
            @RequestParam(required = false, defaultValue = "0") Integer flag) {

        return employeeService.queryEmployeeList(name, currentPage, pageSize, flag);
    }

    /**
     * 保存员工信息
     * @param employee 员工实体
     * @return result
     */
    @PostMapping("/save")
    public Result<Object> saveEmployee(Employee employee) {

        return employeeService.saveEmployee(employee);
    }


}
