package com.ledger.db.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.common.exception.BizException;
import com.ledger.common.result.Result;
import com.ledger.db.entity.Employee;
import com.ledger.db.entity.Job;
import com.ledger.db.mapper.EmployeeMapper;
import com.ledger.db.mapper.JobMapper;
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
public class EmployeeServiceImpl extends LedgerServiceImpl<EmployeeMapper, Employee> implements IEmployeeService {


    private final EmployeeMapper employeeMapper;

    private final JobMapper jobMapper;

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
     * 保存员工信息。
     *
     * <p>姓名重复会触发唯一索引，DuplicateKeyException 由全局异常处理器翻译成
     * 「已存在同名记录」，这里不再 catch——catch 住就等于告诉事务「一切正常」，
     * 该回滚的不会回滚。</p>
     *
     * @param employee 员工实体
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> saveEmployee(Employee employee) {
        if (!save(employee)) {
            throw new BizException("新增员工失败，请重试");
        }
        return Result.ok();
    }

    /**
     * 更新员工信息
     *
     * @param employee 员工实体
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> updateEmployee(Employee employee) {
        if (!updateById(employee)) {
            throw new BizException("修改员工失败：该记录不存在，或已被其他人修改，请刷新后重试");
        }
        return Result.ok();
    }

    /**
     * 删除员工信息（逻辑删除）。
     *
     * <p>删之前先看有没有工作记录：有就拒绝。否则删完员工，历史工资记录里的
     * 员工姓名/工作方式就只剩一个悬空的 employee_id，按月结算的账也就对不上了。</p>
     *
     * @param id 员工ID
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> deleteEmployee(Integer id) {
        Long jobCount = jobMapper.selectCount(Wrappers.<Job>lambdaQuery()
                .eq(Job::getEmployeeId, id)
                .eq(Job::getFlag, 0));
        if (jobCount != null && jobCount > 0) {
            throw new BizException("删除员工失败：该员工还有 " + jobCount
                    + " 条工作记录，删掉后历史工资就查不到人了。请先处理这些记录。");
        }
        if (!removeById(id)) {
            throw new BizException("删除员工失败：该记录不存在或已被删除");
        }
        return Result.ok();
    }

}
