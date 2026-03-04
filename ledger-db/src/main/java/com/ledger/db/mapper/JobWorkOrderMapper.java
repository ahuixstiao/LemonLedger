package com.ledger.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.db.entity.JobWorkOrder;
import com.ledger.db.entity.dto.JobWorkOrderDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 工作工单表 Mapper 接口
 * </p>
 *
 * @author ahui
 * @since 2026-02-26
 */
@Mapper
public interface JobWorkOrderMapper extends BaseMapper<JobWorkOrder> {

    Page<JobWorkOrderDTO> selectWorkOrderListByCondition(
            Page<JobWorkOrderDTO> page,
            @Param("factoryId") Integer factoryId,
            @Param("number") String number,
            @Param("styleNumber") String styleNumber,
            @Param("categoryId") Integer categoryId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("flag") Integer flag
    );

}
