package com.ledger.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.db.entity.JobWorkOrder;
import com.ledger.db.entity.dto.JobWorkOrderDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

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

    /**
     * 压花认领：把「剩余够不够」的判断和扣减放进同一条 UPDATE。
     * 由 InnoDB 的行锁保证并发下只有能放下的那个请求会成功，返回 0 表示剩余不足
     * （或工单已被删除、数量不合法），调用方按「超出可认领数量」处理。
     */
    @Update("UPDATE job_work_order SET claimed_emboss = claimed_emboss + #{quantity} "
            + "WHERE id = #{id} AND flag = 0 AND #{quantity} > 0 "
            + "AND quantity - claimed_emboss >= #{quantity}")
    int increaseClaimedEmboss(@Param("id") Integer id, @Param("quantity") Integer quantity);

    /**
     * 刮胶认领，语义同 {@link #increaseClaimedEmboss(Integer, Integer)}
     */
    @Update("UPDATE job_work_order SET claimed_glue = claimed_glue + #{quantity} "
            + "WHERE id = #{id} AND flag = 0 AND #{quantity} > 0 "
            + "AND quantity - claimed_glue >= #{quantity}")
    int increaseClaimedGlue(@Param("id") Integer id, @Param("quantity") Integer quantity);

    /**
     * 认领记录被改小或删除时，把压花计数减回去（兜底不小于 0）
     */
    @Update("UPDATE job_work_order SET claimed_emboss = GREATEST(claimed_emboss - #{quantity}, 0) WHERE id = #{id}")
    int decreaseClaimedEmboss(@Param("id") Integer id, @Param("quantity") Integer quantity);

    /**
     * 刮胶计数减回去，语义同 {@link #decreaseClaimedEmboss(Integer, Integer)}
     */
    @Update("UPDATE job_work_order SET claimed_glue = GREATEST(claimed_glue - #{quantity}, 0) WHERE id = #{id}")
    int decreaseClaimedGlue(@Param("id") Integer id, @Param("quantity") Integer quantity);

}
