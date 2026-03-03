package com.ledger.db.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ledger.db.entity.JobCategory;


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

}
