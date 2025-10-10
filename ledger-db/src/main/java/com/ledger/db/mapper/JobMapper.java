package com.ledger.db.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.db.entity.Job;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ledger.db.entity.dto.JobDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

}
