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

    /**
     * 查询员工列表
     *
     * @param flag 删除状态 0否 1是
     * @return result
     */
    Result<Object> queryEmployeeList(Integer flag);

    /**
     * 按条件查询员工列表
     *
     * @param name 员工名称
     * @param currentPage 当前页
     * @param pageSize 页面条数
     * @param flag 删除状态 0否 1是
     * @return result
     */
    Result<Object> queryEmployeeList(String name, Integer currentPage, Integer pageSize, Integer flag);

    Result<Object> saveEmployee(Employee employee);
}
