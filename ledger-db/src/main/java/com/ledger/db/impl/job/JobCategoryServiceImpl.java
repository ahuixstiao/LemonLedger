package com.ledger.db.impl.job;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.common.result.Result;
import com.ledger.db.entity.JobCategory;
import com.ledger.db.mapper.JobCategoryMapper;
import com.ledger.db.service.job.IJobCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
public class JobCategoryServiceImpl extends ServiceImpl<JobCategoryMapper, JobCategory> implements IJobCategoryService {

    /**
     * 查询工作类型列表
     *
     * @param category 工作类型
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @param flag 删除状态 0否 1是
     * @return result
     */
    @Override
    public Result<Object> queryCategoryList(String category, Integer currentPage, Integer pageSize, Integer flag) {

        Page<JobCategory> page = new Page<>(currentPage, pageSize);

        page = lambdaQuery()
                .eq(StrUtil.isNotBlank(category), JobCategory::getCategory, category)
                .eq(JobCategory::getFlag, flag)
                .page(page);

        return Result.ok(page);
    }

    /**
     * 保存工作类型
     *
     * @param jobCategory 工作类型实体
     * @return result
     */
    @Override
    public Result<Object> saveJobCategoryInfo(JobCategory jobCategory) {

        if (save(jobCategory)) {
            return Result.ok();
        }

        return Result.fail();
    }

    /**
     * 删除工作类型
     *
     * @param categoryId 工作类型ID
     * @return result
     */
    @Override
    public Result<Object> removeJobCategoryInfo(Integer categoryId) {

        if (removeById(categoryId)) {
            return Result.ok();
        }

        return Result.fail();
    }
}
