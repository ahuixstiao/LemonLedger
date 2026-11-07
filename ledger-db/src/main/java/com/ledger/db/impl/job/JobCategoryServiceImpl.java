package com.ledger.db.impl.job;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.common.exception.BizException;
import com.ledger.common.result.Result;
import com.ledger.db.entity.FactoryPieceRate;
import com.ledger.db.entity.Job;
import com.ledger.db.entity.JobCategory;
import com.ledger.db.entity.JobPieceRate;
import com.ledger.db.entity.JobWorkOrder;
import com.ledger.db.impl.LedgerServiceImpl;
import com.ledger.db.mapper.FactoryPieceRateMapper;
import com.ledger.db.mapper.JobCategoryMapper;
import com.ledger.db.mapper.JobMapper;
import com.ledger.db.mapper.JobPieceRateMapper;
import com.ledger.db.mapper.JobWorkOrderMapper;
import com.ledger.db.service.job.IJobCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 工作类型表 服务实现类
 * </p>
 *
 * @author ahui
 * @since 2025-10-02
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class JobCategoryServiceImpl extends LedgerServiceImpl<JobCategoryMapper, JobCategory> implements IJobCategoryService {

    private final JobCategoryMapper jobCategoryMapper;

    private final JobMapper jobMapper;

    private final JobPieceRateMapper jobPieceRateMapper;

    private final FactoryPieceRateMapper factoryPieceRateMapper;

    private final JobWorkOrderMapper jobWorkOrderMapper;

    /**
     * 查询工作类型列表
     *
     * @param category    工作类型
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @param flag        删除状态 0否 1是
     * @return result
     */
    @Override
    @Transactional(readOnly = true)
    public Result<Object> queryCategoryList(String category, Integer currentPage, Integer pageSize, Integer flag) {

        Page<JobCategory> page = new Page<>(currentPage, pageSize);
        page = jobCategoryMapper.selectJobCategoryListByCondition(page, category, flag);
        return Result.ok(page);
    }

    /**
     * 查询全部未删除的工作类型，给下拉框用
     *
     * @return result
     */
    @Override
    @Transactional(readOnly = true)
    public Result<Object> queryAllCategoryList() {

        return Result.ok(list(Wrappers.<JobCategory>lambdaQuery()
                .eq(JobCategory::getFlag, 0)
                .orderByAsc(JobCategory::getId)));
    }

    /**
     * 保存工作类型
     *
     * @param jobCategory 工作类型实体
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> saveJobCategoryInfo(JobCategory jobCategory) {

        if (!save(jobCategory)) {
            throw new BizException("新增工作类型失败，请重试");
        }

        return Result.ok();
    }

    /**
     * 删除工作类型
     *
     * @param categoryId 工作类型ID
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> removeJobCategoryInfo(Integer categoryId) {

        // 被单据或计件单价引用的工作类型不许删，否则历史账单/工资会失去类型归属
        long jobCount = jobMapper.selectCount(
                Wrappers.<Job>lambdaQuery().eq(Job::getCategoryId, categoryId).eq(Job::getFlag, 0));
        long workOrderCount = jobWorkOrderMapper.selectCount(
                Wrappers.<JobWorkOrder>lambdaQuery().eq(JobWorkOrder::getCategoryId, categoryId).eq(JobWorkOrder::getFlag, 0));
        long jobPieceRateCount = jobPieceRateMapper.selectCount(
                Wrappers.<JobPieceRate>lambdaQuery().eq(JobPieceRate::getCategoryId, categoryId).eq(JobPieceRate::getFlag, 0));
        long factoryPieceRateCount = factoryPieceRateMapper.selectCount(
                Wrappers.<FactoryPieceRate>lambdaQuery().eq(FactoryPieceRate::getCategoryId, categoryId).eq(FactoryPieceRate::getFlag, 0));

        if (jobCount > 0 || workOrderCount > 0 || jobPieceRateCount > 0 || factoryPieceRateCount > 0) {
            throw new BizException("该工作类型还有 " + jobCount + " 条工作记录、"
                    + workOrderCount + " 张工单、" + jobPieceRateCount + " 条工资计件单价、"
                    + factoryPieceRateCount + " 条出厂计件单价，不能直接删除");
        }

        if (!removeById(categoryId)) {
            throw new BizException("删除工作类型失败：该类型不存在或已被删除");
        }

        return Result.ok();
    }
}
