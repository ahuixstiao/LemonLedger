package com.ledger.api.controller.employee;

import com.ledger.common.result.Result;
import com.ledger.db.entity.Employee;
import com.ledger.db.service.IEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 员工只读接口：给下拉框用（员工端认领、筛选时取全量在职员工）。
 *
 * <p>员工的增删改统一在 {@link com.ledger.api.controller.admin.AdminEmployeeController}（/admin/emp）里，
 * 原来这里还有一个 /emp/save，和 admin 的 POST /admin/emp 是同一件事的两份实现，
 * 改其中一份很容易漏掉另一份，已删除（前端没有任何地方调用 /emp/save）。</p>
 *
 * @Author: ahui
 * @DateTime: 2025/10/21 - 20:48
 **/
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/emp")
public class EmployeeController {

    private final IEmployeeService employeeService;

    /**
     * 获取全部在职员工（不分页，供下拉框使用）
     *
     * @return result
     */
    @GetMapping("/list")
    public Result<Object> queryEmployeeList() {
        return Result.ok(employeeService.lambdaQuery().eq(Employee::getFlag, 0).list());
    }
}
