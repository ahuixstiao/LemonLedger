package com.ledger.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.db.entity.Job;
import com.ledger.db.entity.dto.JobDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 工作内容表 Mapper 接口
 * </p>
 *
 * @author ahui
 * @since 2025-10-02
 */
@Mapper
public interface JobMapper extends BaseMapper<Job> {

    /**
     * 根据员工ID查询员工工作信息
     *
     * @param page       分页参数
     * @param employeeId 员工ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @param factoryId   指定工厂
     * @param number      指定床号
     * @param categoryId  指定工作类型
     * @return 以分页后的方式返回工作信息列表
     */
    Page<JobDTO> selectJobListByCondition(
            Page<JobDTO> page,
            @Param("employeeId") Integer employeeId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("factoryId") Integer factoryId,
            @Param("number") String number,
            @Param("categoryId") Integer categoryId,
            @Param("flag") Integer flag);


    /**
     * 根据员工ID统计薪水
     * @param employeeId 员工ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param flag 删除状态 0否 1是
     * @return BigDecimal 薪水总额
     */
    JobDTO calculateSalaryByEmployeeIdAndDate(
            @Param("employeeId") Integer employeeId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("flag") Integer flag);

    /**
     * 统计某个工单在某种工作方式下已被认领的数量。
     *
     * <p>用 SUM() 在数据库里算，而不是把明细 list 查回内存再 stream 求和：
     * 一个工单可能被认领几百上千次，全查回来只为加一个数既费内存又费时间；
     * 而且一边查一边加，读到的是「查的那一刻」的快照，配合下面的行锁才是一个原子判断。</p>
     *
     * @param workOrderId 工单ID
     * @param modeId      工作方式ID
     * @return 已认领数量，没有记录时返回 0（COALESCE 保证不会是 null）
     */
    @Select("SELECT COALESCE(SUM(quantity), 0) FROM job"
            + " WHERE work_order_id = #{workOrderId} AND mode_id = #{modeId} AND flag = 0")
    Integer sumClaimedQuantityByMode(
            @Param("workOrderId") Integer workOrderId,
            @Param("modeId") Integer modeId);

}
