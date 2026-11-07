package com.ledger.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ledger.db.entity.Batch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 床 Mapper
 * </p>
 *
 * @author ahui
 * @since 2026-11-06
 */
@Mapper
public interface BatchMapper extends BaseMapper<Batch> {

    /**
     * 床号/款式改了之后，把工作记录上的冗余列同步过来。
     * <p>
     * 冗余列是当年为了少 join 才抄的，现在留着不动（报表 SQL 还在用），
     * 但只要 batch 是唯一事实来源，就得在有改动时把它们刷新一遍。
     * </p>
     *
     * @param batchId     床ID
     * @param factoryId   成衣厂ID
     * @param number      床号
     * @param styleNumber 款式编号
     * @return 影响行数
     */
    @Update("UPDATE job SET factory_id = #{factoryId}, number = #{number}, style_number = #{styleNumber} " +
            "WHERE batch_id = #{batchId}")
    int updateJobBatchInfo(@Param("batchId") Integer batchId,
                           @Param("factoryId") Integer factoryId,
                           @Param("number") String number,
                           @Param("styleNumber") String styleNumber);

    /**
     * 同步工单上的冗余列
     *
     * @param batchId     床ID
     * @param factoryId   成衣厂ID
     * @param number      床号
     * @param styleNumber 款式编号
     * @return 影响行数
     */
    @Update("UPDATE job_work_order SET factory_id = #{factoryId}, number = #{number}, style_number = #{styleNumber} " +
            "WHERE batch_id = #{batchId}")
    int updateWorkOrderBatchInfo(@Param("batchId") Integer batchId,
                                 @Param("factoryId") Integer factoryId,
                                 @Param("number") String number,
                                 @Param("styleNumber") String styleNumber);

    /**
     * 同步账单上的冗余列（账单没有款式编号列，款式从计件单价来）
     *
     * @param batchId   床ID
     * @param factoryId 成衣厂ID
     * @param number    床号
     * @return 影响行数
     */
    @Update("UPDATE factory_bill SET factory_id = #{factoryId}, number = #{number} WHERE batch_id = #{batchId}")
    int updateBillBatchInfo(@Param("batchId") Integer batchId,
                            @Param("factoryId") Integer factoryId,
                            @Param("number") String number);
}
