package com.ledger.db.service.impl;

import com.ledger.db.entity.Employee;
import com.ledger.db.mapper.EmployeeMapper;
import com.ledger.db.service.IEmployeeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 员工表 服务实现类
 * </p>
 *
 * @author ahui
 * @since 2025-10-02
 */
@Service
@RequiredArgsConstructor(onConstructor_ =  @Autowired)
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements IEmployeeService {

    @Override
    public List<Employee> queryEmployees(){
        // 查询正常的员工列表
        return lambdaQuery().eq(Employee::getFlag, 0).list();
    }


}
