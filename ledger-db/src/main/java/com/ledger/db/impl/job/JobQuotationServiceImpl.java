package com.ledger.db.impl.job;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ledger.common.result.Result;
import com.ledger.db.entity.JobQuotation;
import com.ledger.db.mapper.JobQuotationMapper;
import com.ledger.db.service.job.IJobQuotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 工作报价表 服务实现类
 * </p>
 *
 * @author ahui
 * @since 2025-10-16
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class JobQuotationServiceImpl extends ServiceImpl<JobQuotationMapper, JobQuotation> implements IJobQuotationService {

    /**
     * 按条件分页查询工作报价列表。
     */
    @Override
    public Result<Object> queryJobQuotationListByCondition(
            Integer modeId,
            Integer categoryId,
            Integer currentPage,
            Integer pageSize,
            Integer flag
    ) {
        Page<JobQuotation> page = lambdaQuery()
                .eq(ObjectUtil.isNotEmpty(modeId), JobQuotation::getModeId, modeId)
                .eq(ObjectUtil.isNotEmpty(categoryId), JobQuotation::getCategoryId, categoryId)
                .eq(JobQuotation::getFlag, flag)
                .page(new Page<>(currentPage, pageSize));
        return Result.ok(page);
    }

    /**
     * 保存工作报价。
     */
    @Override
    public Result<Object> saveJobQuotationInfo(JobQuotation jobQuotation) {
        return save(jobQuotation) ? Result.ok() : Result.fail();
    }

    /**
     * 修改工作报价。
     */
    @Override
    public Result<Object> updateJobQuotationInfo(JobQuotation jobQuotation) {
        return updateById(jobQuotation) ? Result.ok() : Result.fail();
    }

    /**
     * 删除工作报价。
     */
    @Override
    public Result<Object> removeJobQuotationInfo(Integer id) {
        return removeById(id) ? Result.ok() : Result.fail();
    }
}
