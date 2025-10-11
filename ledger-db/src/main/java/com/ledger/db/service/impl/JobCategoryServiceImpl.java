package com.ledger.db.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.common.result.Result;
import com.ledger.db.entity.JobCategory;
import com.ledger.db.mapper.JobCategoryMapper;
import com.ledger.db.service.IJobCategoryService;
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

    private final JobCategoryMapper jobCategoryMapper;

    /**
     * 查询工作类型列表
     *
     * @param category 工作类型
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @return result
     */
    @Override
    public Result<Object> queryCategoryList(String category, Integer currentPage, Integer pageSize, Integer flag) {

        return Result.ok(jobCategoryMapper.queryCategoryList(new Page<>(currentPage, pageSize), null));
    }
}
