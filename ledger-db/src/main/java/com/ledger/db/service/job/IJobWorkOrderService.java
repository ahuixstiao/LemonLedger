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
}
