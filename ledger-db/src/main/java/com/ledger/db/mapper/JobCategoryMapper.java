package com.ledger.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.db.entity.JobCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 工作类型表 Mapper 接口
 * </p>
 *
 * @author ahui
 * @since 2025-10-02
 */
@Mapper
public interface JobCategoryMapper extends BaseMapper<JobCategory> {

    /**
     * 按条件分页查询工作类型列表（自定义SQL，支持按flag查询逻辑删除数据）。
     *
     * @param page     分页对象
     * @param category 工作类型
     * @param flag     删除状态
     * @return page
     */
    Page<JobCategory> selectJobCategoryListByCondition(
            Page<JobCategory> page,
            @Param("category") String category,
            @Param("flag") Integer flag
    );
}
