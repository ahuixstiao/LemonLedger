package com.ledger.common.dto.employee;

import lombok.Data;

/**
 * 员工认领工单参数。
 */
@Data
public class WorkOrderClaimDTO {

    /**
     * 工单ID。
     */
    private Integer workOrderId;

    /**
     * 员工ID。
     */
    private Integer employeeId;

    /**
     * 认领数量。
     */
    private Integer quantity;
}
