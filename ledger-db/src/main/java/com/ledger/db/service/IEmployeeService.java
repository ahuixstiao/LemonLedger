package com.ledger.db.service;

import com.ledger.db.entity.Employee;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Mapper;

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
    List<Employee> queryEmployees();
}
