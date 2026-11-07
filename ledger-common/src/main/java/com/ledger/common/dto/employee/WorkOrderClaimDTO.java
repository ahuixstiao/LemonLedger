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

    /**
     * 工作方式ID。
     * 一个人可能同时会压花和刮胶（员工档案上不再强制绑定唯一工作方式），
     * 所以认领时由前端明确指定本次按哪种方式计工资；不传则退回员工档案里配置的默认方式。
     */
    private Integer modeId;
}
