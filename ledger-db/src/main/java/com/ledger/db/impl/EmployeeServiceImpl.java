package com.ledger.db.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.common.result.Result;
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

    /**
     * 查询员工列表
     * @return result
     */
    @Override
    public Result<Object> queryEmployeeList(String name, Integer currentPage, Integer pageSize, Integer flag){
        // 构建分页对象
        Page<Employee> page = new Page<>(currentPage, pageSize);

        lambdaQuery()
                .eq(Employee::getFlag, flag)
                // 如果传入名字就按名字搜索
                .eq(StrUtil.isNotBlank(name), Employee::getName, name)
                .page(page);
        return Result.ok(page);
    }

    /**
     * 保存员工信息
     * @param employee 员工实体
     * @return result
     */
    @Override
    public Result<Object> saveEmployee(Employee employee) {

        if (save(employee)) {
            return Result.ok();
        }

        return Result.fail();
    }


}
