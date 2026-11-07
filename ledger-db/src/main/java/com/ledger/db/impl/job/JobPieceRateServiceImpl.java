package com.ledger.db.impl.job;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.common.exception.BizException;
import com.ledger.common.result.Result;
import com.ledger.db.entity.JobPieceRate;
import com.ledger.db.entity.dto.JobPieceRateDTO;
import com.ledger.db.impl.LedgerServiceImpl;
import com.ledger.db.mapper.JobPieceRateMapper;
import com.ledger.db.service.job.IJobPieceRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 员工计件单价表 服务实现类
 * </p>
 *
 * @author ahui
 * @since 2025-10-16
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class JobPieceRateServiceImpl extends LedgerServiceImpl<JobPieceRateMapper, JobPieceRate> implements IJobPieceRateService {

    private final JobPieceRateMapper jobPieceRateMapper;

    /**
     * 按条件分页查询员工计件单价列表。
     */
    @Override
    @Transactional(readOnly = true)
    public Result<Object> queryJobPieceRateListByCondition(
            Integer modeId,
            Integer categoryId,
            Integer currentPage,
            Integer pageSize,
            Integer flag
    ) {
        Page<JobPieceRateDTO> page = new Page<>(currentPage, pageSize);
        page = jobPieceRateMapper.selectJobPieceRateListByCondition(page, modeId, categoryId, flag);
        return Result.ok(page);
    }

    /**
     * 保存员工计件单价。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> saveJobPieceRateInfo(JobPieceRate jobPieceRate) {
        if (!save(jobPieceRate)) {
            throw new BizException("新增员工计件单价失败，请重试");
        }
        return Result.ok();
    }

    /**
     * 修改员工计件单价。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> updateJobPieceRateInfo(JobPieceRate jobPieceRate) {
        if (!updateById(jobPieceRate)) {
            throw new BizException("修改员工计件单价失败：该计件单价不存在，或已被其他人修改，请刷新后重试");
        }
        return Result.ok();
    }

    /**
     * 删除员工计件单价。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> removeJobPieceRateInfo(Integer id) {
        if (!removeById(id)) {
            throw new BizException("删除员工计件单价失败：该计件单价不存在或已被删除");
        }
        return Result.ok();
    }
}
