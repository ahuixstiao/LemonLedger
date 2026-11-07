package com.ledger.db.service.job;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ledger.common.result.Result;
import com.ledger.db.entity.JobWorkOrder;

/**
 * <p>
 * 工作工单表 服务类
 * </p>
 *
 * @author ahui
 * @since 2026-02-26
 */
public interface IJobWorkOrderService extends IService<JobWorkOrder> {

    /**
     * 按条件分页查询工单列表
     */
    Result<Object> queryWorkOrderListByCondition(
            Integer factoryId,
            String number,
            String styleNumber,
            Integer categoryId,
            String startDate,
            String endDate,
            Integer currentPage,
            Integer pageSize,
            Integer flag
    );

    /**
     * 保存工单
     */
    Result<Object> saveWorkOrder(JobWorkOrder workOrder);

    /**
     * 修改工单
     */
    Result<Object> updateWorkOrder(JobWorkOrder workOrder);

    /**
     * 删除工单
     */
    Result<Object> deleteWorkOrder(Integer id);

    /**
     * 查询一个工单在指定工作方式下还能认领多少。
     *
     * @param workOrderId 工单ID
     * @param employeeId  员工ID，可为空；不为空时用员工档案上的默认工作方式
     * @param modeId      工作方式ID，优先于员工默认方式
     * @return 预设数量、已认领数量、剩余数量
     */
    Result<Object> queryWorkOrderProgress(Integer workOrderId, Integer employeeId, Integer modeId);
}
