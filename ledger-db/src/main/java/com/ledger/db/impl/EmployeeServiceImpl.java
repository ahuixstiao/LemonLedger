package com.ledger.db.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ledger.common.result.Result;
import com.ledger.db.entity.Employee;
import com.ledger.db.mapper.EmployeeMapper;
import com.ledger.db.service.IEmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 员工表 服务实现类
 * </p>
 *
 * @author ahui
 * @since 2025-10-02
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ =  @Autowired)
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements IEmployeeService {


    private final EmployeeMapper employeeMapper;

    /**
     * 按条件查询员工列表 默认查全部员工
     *
     * @param name 员工名称
     * @param currentPage 当前页
     * @param pageSize 页面条数
     * @param flag 删除状态 0否 1是
     * @return result
     */
    @Override
    public Result<Object> queryEmployeeList(String name, Integer currentPage, Integer pageSize, Integer flag) {
        // 构建分页对象
        Page<Employee> page = new Page<>(currentPage, pageSize);

        // 使用自定义 SQL，绕过 MyBatis-Plus 逻辑删除自动过滤
        page = employeeMapper.queryEmployeeList(page, name, flag);

        return Result.ok(page);
    }

    /**
     * 保存员工信息
     * @param employee 员工实体
     * @return result
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Object> saveEmployee(Employee employee) {

        //处理重复名称的异常情况
        try {
            if (save(employee)) {
                return Result.ok();
            }
        } catch (RuntimeException runtimeException) {
            // 打印异常
            log.error(runtimeException.getMessage());
        }

        return Result.fail();
    }

    /**
     * 更新员工信息
     *
     * @param employee 员工实体
     * @return result
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Object> updateEmployee(Employee employee) {
        try {
            if (updateById(employee)) {
                return Result.ok();
            }
        } catch (RuntimeException runtimeException) {
            log.error(runtimeException.getMessage());
        }
        return Result.fail();
    }

    /**
     * 删除员工信息（逻辑删除）
     *
     * @param id 员工ID
     * @return result
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Object> deleteEmployee(Integer id) {
        try {
            if (removeById(id)) {
                return Result.ok();
            }
        } catch (RuntimeException runtimeException) {
            log.error(runtimeException.getMessage());
        }
        return Result.fail();
    }

}
