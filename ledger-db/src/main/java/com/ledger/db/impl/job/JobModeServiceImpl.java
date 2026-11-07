package com.ledger.db.impl.job;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.common.exception.BizException;
import com.ledger.common.result.Result;
import com.ledger.db.entity.Employee;
import com.ledger.db.entity.Job;
import com.ledger.db.entity.JobMode;
import com.ledger.db.entity.JobPieceRate;
import com.ledger.db.impl.LedgerServiceImpl;
import com.ledger.db.mapper.EmployeeMapper;
import com.ledger.db.mapper.JobMapper;
import com.ledger.db.mapper.JobModeMapper;
import com.ledger.db.mapper.JobPieceRateMapper;
import com.ledger.db.service.job.IJobModeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 工作方式表 服务实现类
 * </p>
 *
 * @author ahui
 * @since 2025-10-02
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class JobModeServiceImpl extends LedgerServiceImpl<JobModeMapper, JobMode> implements IJobModeService {

    private final JobMapper jobMapper;

    private final JobPieceRateMapper jobPieceRateMapper;

    private final EmployeeMapper employeeMapper;

    /**
     * 查询工作方式列表
     *
     * @param currentPage 当前页
     * @param pageSize    页面条数
     * @param mode        工作方式
     * @return result
     */
    @Override
    public Result<Object> queryJobModeList(Integer currentPage, Integer pageSize, String mode, Integer flag) {

        Page<JobMode> page = new Page<>(currentPage, pageSize);
        String queryMode = StrUtil.trim(mode);
        Integer queryFlag = flag == null ? 0 : flag;

        Page<JobMode> result = baseMapper.queryJobModeList(page, queryMode, queryFlag);
        return Result.ok(result);
    }

    /**
     * 保存工作方式信息
     *
     * @param jobMode 工作方式实体
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> saveJobModeInfo(JobMode jobMode) {

        if (!save(jobMode)) {
            throw new BizException("新增工作方式失败，请重试");
        }

        return Result.ok();
    }

    /**
     * 删除工作方式信息
     *
     * @param jobModeId 工作方式ID
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> removeJobModeInfo(Integer jobModeId) {

        // 有计件单价、员工或工作记录还在用这个工作方式时不许删：
        // 删掉之后这些记录里的 mode_id 指向空，工资计算（类型+方式查计件单价）会直接找不到单价。
        long priceCount = jobPieceRateMapper.selectCount(
                Wrappers.<JobPieceRate>lambdaQuery().eq(JobPieceRate::getModeId, jobModeId).eq(JobPieceRate::getFlag, 0));
        long employeeCount = employeeMapper.selectCount(
                Wrappers.<Employee>lambdaQuery().eq(Employee::getModeId, jobModeId).eq(Employee::getFlag, 0));
        long jobCount = jobMapper.selectCount(
                Wrappers.<Job>lambdaQuery().eq(Job::getModeId, jobModeId).eq(Job::getFlag, 0));

        if (priceCount > 0 || employeeCount > 0 || jobCount > 0) {
            throw new BizException("该工作方式还有 " + priceCount + " 条计件单价、"
                    + employeeCount + " 名员工默认使用、"
                    + jobCount + " 条工作记录，不能直接删除");
        }

        if (!removeById(jobModeId)) {
            throw new BizException("删除工作方式失败：该工作方式不存在或已被删除");
        }

        return Result.ok();
    }
}
