package com.ledger.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.db.entity.JobPieceRate;
import com.ledger.db.entity.dto.JobPieceRateDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 员工计件单价表 Mapper 接口
 * </p>
 *
 * @author ahui
 * @since 2025-10-16
 */
@Mapper
public interface JobPieceRateMapper extends BaseMapper<JobPieceRate> {

    /**
     * 按条件分页查询员工计件单价列表（自定义SQL，支持按flag查询逻辑删除数据）。
     *
     * @param page       分页对象
     * @param modeId     工作方式ID
     * @param categoryId 工作类型ID
     * @param flag       删除状态
     * @return page
     */
    Page<JobPieceRateDTO> selectJobPieceRateListByCondition(
            Page<JobPieceRateDTO> page,
            @Param("modeId") Integer modeId,
            @Param("categoryId") Integer categoryId,
            @Param("flag") Integer flag
    );
}
