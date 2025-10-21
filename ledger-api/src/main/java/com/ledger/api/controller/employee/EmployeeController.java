package com.ledger.api.controller.employee;

import com.ledger.common.result.Result;
import com.ledger.db.entity.Employee;
import com.ledger.db.service.IEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: ahui
 * @Description: TODO 员工控制器
 * @DateTime: 2025/10/21 - 20:48
 **/
@RestController
@RequiredArgsConstructor(onConstructor_ =  @Autowired)
@RequestMapping("/emp")
public class EmployeeController {

    private final IEmployeeService employeeService;

    /**
     * 获取员工列表
     *
     * @return result
     */
    @GetMapping("/list")
    public Result<Object> queryEmployeeList() {

        return Result.ok(employeeService.lambdaQuery().eq(Employee::getFlag, 0).list());
    }

    /**
     * 保存员工信息
     *
     * @param employee 员工实体
     * @return result
     */
    @PostMapping("/save")
    public Result<Object> saveEmployee(@RequestBody Employee employee) {

        return employeeService.saveEmployee(employee);
    }


}
