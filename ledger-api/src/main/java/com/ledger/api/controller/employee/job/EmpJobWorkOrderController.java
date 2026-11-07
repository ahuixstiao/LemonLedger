package com.ledger.api.controller.employee.job;

import com.ledger.common.result.Result;
import com.ledger.db.entity.JobWorkOrder;
import com.ledger.db.service.job.IJobWorkOrderService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * 工作工单表 前端控制器
 * </p>
 *
 * @author ahui
 * @since 2026-02-26
 */
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/emp/workOrder")
@Validated
public class EmpJobWorkOrderController {

    private final IJobWorkOrderService jobWorkOrderService;

    /**
     * 按条件分页查询工单列表
     */
    @GetMapping
    public Result<Object> queryWorkOrderList(
            @RequestParam(required = false) Integer factoryId,
            @RequestParam(required = false) String number,
            @RequestParam(required = false) String styleNumber,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "1") Integer currentPage,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "0") Integer flag
    ) {
        return jobWorkOrderService.queryWorkOrderListByCondition(
                factoryId, number, styleNumber, categoryId,
                startDate, endDate, currentPage, pageSize, flag
        );
    }

    /**
     * 新增工单
     */
    @PostMapping
    public Result<Object> saveWorkOrder(@RequestBody JobWorkOrder workOrder) {
        return jobWorkOrderService.saveWorkOrder(workOrder);
    }

    /**
     * 修改工单
     */
    @PutMapping("/{id}")
    public Result<Object> updateWorkOrder(@PathVariable @NotNull Integer id, @RequestBody JobWorkOrder workOrder) {
        workOrder.setId(id);
        return jobWorkOrderService.updateWorkOrder(workOrder);
    }

    /**
     * 删除工单
     */
    @DeleteMapping("/{id}")
    public Result<Object> deleteWorkOrder(@PathVariable @NotNull Integer id) {
        return jobWorkOrderService.deleteWorkOrder(id);
    }

    /**
     * 查询某张工单在某种工作方式下还能认领多少。
     *
     * <p>认领弹窗打开时先调这个接口，把「剩余可认领」显示出来，
     * 员工不用先猜一个数字再被驳回来。</p>
     *
     * @param id         工单ID
     * @param employeeId 员工ID（可选，用来取员工档案里的默认工作方式）
     * @param modeId     工作方式ID（可选，优先级高于员工默认方式）
     */
    @GetMapping("/{id}/progress")
    public Result<Object> queryWorkOrderProgress(
            @PathVariable @NotNull Integer id,
            @RequestParam(required = false) Integer employeeId,
            @RequestParam(required = false) Integer modeId) {
        return jobWorkOrderService.queryWorkOrderProgress(id, employeeId, modeId);
    }
}
