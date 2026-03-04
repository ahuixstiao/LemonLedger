package com.ledger.api.controller.admin;

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
     * @param queryDTO 查询条件
     * @return result
     */
    @GetMapping
    public Result<Object> queryEmployees(EmployeeQueryDTO queryDTO) {
        return employeeService.queryEmployeeList(
                queryDTO.getName() == null ? null : queryDTO.getName().trim(),
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

    /**
     * 更新员工信息
     *
     * @param id 员工ID
     * @param employee 员工实体
     * @return result
     */
    @PutMapping("/{id}")
    public Result<Object> updateEmployee(@PathVariable Integer id, @RequestBody Employee employee) {
        employee.setId(id);
        return employeeService.updateEmployee(employee);
    }

    /**
     * 删除员工信息（逻辑删除）
     *
     * @param id 员工ID
     * @return result
     */
    @DeleteMapping("/{id}")
    public Result<Object> deleteEmployee(@PathVariable Integer id) {
        return employeeService.deleteEmployee(id);
    }

}
