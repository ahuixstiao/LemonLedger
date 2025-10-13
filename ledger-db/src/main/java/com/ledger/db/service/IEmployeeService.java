package com.ledger.db.service;

import com.ledger.common.result.Result;
import com.ledger.db.entity.Employee;
import com.baomidou.mybatisplus.extension.service.IService;


import java.util.List;

/**
 * <p>
 * 员工表 服务类
 * </p>
 *
 * @author ahui
 * @since 2025-10-02
 */
public interface IEmployeeService extends IService<Employee> {

    /*
    * 查询员工列表
    * */
    Result<Object> queryEmployeeList(String name, Integer currentPage, Integer pageSize, Integer flag);


    Result<Object> saveEmployee(Employee employee);
}
