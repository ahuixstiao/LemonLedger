package com.ledger.db.service;

import com.ledger.common.result.Result;
import com.ledger.db.entity.Employee;
import com.baomidou.mybatisplus.extension.service.IService;

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
     * 按条件分页查询员工列表
     *
     * @param name 员工名称
     * @param currentPage 当前页
     * @param pageSize 页面条数
     * @param flag 删除状态 0否 1是
     * @return result
     */
    Result<Object> queryEmployeeList(String name, Integer currentPage, Integer pageSize, Integer flag);

    /**
     * 保存员工信息
     *
     * @param employee 员工实体
     * @return result
     */
    Result<Object> saveEmployee(Employee employee);

    /**
     * 更新员工信息
     *
     * @param employee 员工实体
     * @return result
     */
    Result<Object> updateEmployee(Employee employee);

    /**
     * 删除员工信息（逻辑删除）
     *
     * @param id 员工ID
     * @return result
     */
    Result<Object> deleteEmployee(Integer id);
}
