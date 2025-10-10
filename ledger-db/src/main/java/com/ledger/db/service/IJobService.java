package com.ledger.db.service;

import com.ledger.common.result.Result;
import com.ledger.db.entity.Job;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;

/**
 * <p>
 * 工作内容表 服务类
 * </p>
 *
 * @author ahui
 * @since 2025-10-02
 */
public interface IJobService extends IService<Job> {

    // 查询员工 指定时间内的工作信息
    Result<Object> queryJobByDate(Integer empId, String startTime, String endTime, Integer currentPage, Integer pageSize);

    // 员工添加工作记录
    Result<Object> addJobRecord(Job job);

    // 员工修改工作记录
    Result<Object> updateJobRecord(Job job);
}
