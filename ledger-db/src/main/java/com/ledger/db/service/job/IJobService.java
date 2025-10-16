package com.ledger.db.service.job;

import com.ledger.common.result.Result;
import com.ledger.db.entity.Job;
import com.baomidou.mybatisplus.extension.service.IService;

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
     * 如果传入员工ID 则按员工ID 查询当天 或 由员工指定的起止日期的工作信息
     *
     * @param employeeId       员工ID
     * @param startDate   开始时间
     * @param endDate     结束时间
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @param flag        删除状态 0否 1是
     * @return result
     */
    Result<Object> queryJobListByEmployeeIDAndDate(Integer employeeId, String startDate, String endDate, Integer currentPage, Integer pageSize, Integer flag);

    /**
     * 统计员工薪水
     *
     * @param employeeId 员工ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param flag       删除状态 0否 1是
     * @return result
     */
    // 统计薪水
    Result<Object> statisticalSalary(Integer employeeId, String startTime, String endTime, Integer flag);

    // 员工添加工作记录
    Result<Object> saveJobInfo(Job job);

    // 员工修改工作记录
    Result<Object> updateJobInfo(Job job);

    /**
     * 删除工作信息
     * @param jobId 工作ID
     * @return result
     */
    Result<Object> deleteJobInfo(Integer jobId);
}
