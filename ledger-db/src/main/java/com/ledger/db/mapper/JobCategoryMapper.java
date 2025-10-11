package com.ledger.db.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.common.result.Result;
import com.ledger.db.entity.JobCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ledger.db.entity.dto.JobCategoryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.NonNull;

import javax.validation.constraints.Null;

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
     * 查询工作类型列表
     *
     * @param page     分页对象
     * @param category 工作类别
     * @return result
     */
    Result<JobCategoryDTO> queryCategoryList(Page<JobCategoryDTO> page, @Param("category") String category);

}
