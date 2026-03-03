package com.ledger.api.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.ledger.common.dto.admin.EmployeeQueryDTO;
import com.ledger.common.result.Result;
import com.ledger.db.entity.Employee;
import com.ledger.db.service.IEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: ahui
 * @Description: TODO 员工控制器
 * @DateTime: 2025/10/2 - 20:20
 **/
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/admin/emp")
public class AdminEmployeeController {

    private final IEmployeeService employeeService;

    /**
     * 查询员工列表
     *
     * @param flag 删除状态 0否 1是
     * @return result
     */
    @GetMapping
    public Result<Object> queryEmployees(EmployeeQueryDTO queryDTO) {
        if (StrUtil.isBlank(queryDTO.getName())) {
            return employeeService.queryEmployeeList(queryDTO.getFlag());
        }
        return employeeService.queryEmployeeListByCondition(
                queryDTO.getName().trim(),
                queryDTO.getCurrentPage(),
                queryDTO.getPageSize(),
                queryDTO.getFlag()
        );
    }

    /**
     * 保存员工信息
     *
     * @param employee 员工实体
     * @return result
     */
    @PostMapping
    public Result<Object> saveEmployee(@RequestBody Employee employee) {
        return employeeService.saveEmployee(employee);
    }


}
