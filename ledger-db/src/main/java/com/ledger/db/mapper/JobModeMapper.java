package com.ledger.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.db.entity.JobMode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 工作方式表 Mapper 接口
 * </p>
 *
 * @author ahui
 * @since 2025-10-02
 */
@Mapper
public interface JobModeMapper extends BaseMapper<JobMode> {

    /**
     * 自定义 SQL 按条件分页查询工作方式。
     *
     * @param page 分页对象
     * @param mode 工作方式
     * @param flag 状态 0正常 1删除
     * @return 分页结果
     */
    Page<JobMode> queryJobModeList(Page<JobMode> page,
                                   @Param("mode") String mode,
                                   @Param("flag") Integer flag);

}
