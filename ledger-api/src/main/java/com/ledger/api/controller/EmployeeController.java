package com.ledger.api.controller;

import com.ledger.common.result.Result;
import com.ledger.db.entity.Employee;
import com.ledger.db.service.IEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: ahui
 * @Description: TODO 员工控制器
 * @DateTime: 2025/10/2 - 20:20
 **/
@RestController
@RequiredArgsConstructor(onConstructor_ =  @Autowired)
@RequestMapping("/emp")
public class EmployeeController {

    private final IEmployeeService employeeService;


    @GetMapping("/list")
    public Result<Object> queryEmployees() {

        return null;
    }

    //查询员工指定日期的工作信息，默认为当天。
    @GetMapping("/info")
    public String queryEmployeeById(Integer id) {

        return "";
    }

    //查询员工当月工作信息，默认为当月。


    //员工添加工作记录


    //员工修改工作记录



}
