package com.ledger.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.db.entity.Job;
import com.ledger.db.entity.dto.JobDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

}
