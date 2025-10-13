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

    /**
     * 默认查询当天所有员工的工作信息
     *
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @return result
     */
    Result<Object> queryJobListByDefaultCurrentDay(Integer currentPage, Integer pageSize);

    /**
     * 查询指定员工当天或某天的工作信息
     *
     * @param empId       员工ID
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @return result
     */
    Result<Object> queryJobListByEmployeeIDAndDate(Integer empId, String startTime, String endTime, Integer currentPage, Integer pageSize);

    // 员工添加工作记录
    Result<Object> saveJobInfo(Job job);

    // 员工修改工作记录
    Result<Object> updateJobInfo(Job job);
}
