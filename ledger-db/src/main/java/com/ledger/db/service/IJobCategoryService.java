package com.ledger.db.service;

import com.ledger.common.result.Result;
import com.ledger.db.entity.JobCategory;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 工作类型表 服务类
 * </p>
 *
 * @author ahui
 * @since 2025-10-02
 */
public interface IJobCategoryService extends IService<JobCategory> {

    /**
     * 查询工作类型列表
     *
     * @param category    工作类型
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @param flag        删除状态 0未删除 1删除
     * @return result
     */
    Result<Object> queryCategoryList(String category, Integer currentPage, Integer pageSize, Integer flag);

}
