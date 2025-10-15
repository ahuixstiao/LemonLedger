package com.ledger.db.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.db.entity.Job;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ledger.db.entity.dto.JobDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

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
     * 默认查询当天所有员工的工作信息
     *
     * @param page 分页参数
     * @param flag 标志 0表示查询未删除的信息，1表示查询已删除的信息
     * @return 以分页后的方式返回所有员工当天的工作信息列表
     */
    Page<JobDTO> selectJobListByDefaultCurrentDay(Page<JobDTO> page, @Param("flag") Integer flag);

    /**
     * 根据员工ID查询员工工作信息
     *
     * @param page       分页参数
     * @param employeeId 员工ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 以分页后的方式返回工作信息列表
     */
    Page<JobDTO> selectJobListByEmployeeId(
            Page<JobDTO> page,
            @Param("employeeId") Integer employeeId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
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

}
