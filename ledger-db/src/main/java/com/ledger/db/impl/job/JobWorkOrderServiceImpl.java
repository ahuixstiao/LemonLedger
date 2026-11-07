package com.ledger.db.impl.job;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.common.exception.BizException;
import com.ledger.common.result.Result;
import com.ledger.db.entity.Batch;
import com.ledger.db.entity.Employee;
import com.ledger.db.entity.Job;
import com.ledger.db.entity.JobWorkOrder;
import com.ledger.db.entity.dto.JobWorkOrderDTO;
import com.ledger.db.entity.dto.JobWorkOrderProgressDTO;
import com.ledger.db.impl.LedgerServiceImpl;
import com.ledger.db.mapper.JobMapper;
import com.ledger.db.mapper.JobWorkOrderMapper;
import com.ledger.db.service.IBatchService;
import com.ledger.db.service.IEmployeeService;
import com.ledger.db.service.job.IJobWorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * <p>
 * 工作工单表 服务实现类
 * </p>
 *
 * @author ahui
 * @since 2026-02-26
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class JobWorkOrderServiceImpl extends LedgerServiceImpl<JobWorkOrderMapper, JobWorkOrder> implements IJobWorkOrderService {

    private final JobWorkOrderMapper jobWorkOrderMapper;

    private final JobMapper jobMapper;

    private final IEmployeeService employeeService;

    private final IBatchService batchService;

    /**
     * 按条件分页查询工单列表。
     *
     * @param factoryId 成衣厂ID
     * @param number 床号
     * @param styleNumber 款式编号
     * @param categoryId 工作类型ID
     * @param startDate 查询开始日期（含）
     * @param endDate 查询结束日期（含）
     * @param currentPage 当前页
     * @param pageSize 每页条数
     * @param flag 逻辑标识
     * @return 分页后的工单列表
     */
    @Override
    @Transactional(readOnly = true)
    public Result<Object> queryWorkOrderListByCondition(
            Integer factoryId,
            String number,
            String styleNumber,
            Integer categoryId,
            String startDate,
            String endDate,
            Integer currentPage,
            Integer pageSize,
            Integer flag
    ) {
        Page<JobWorkOrderDTO> page = jobWorkOrderMapper.selectWorkOrderListByCondition(
                new Page<>(currentPage, pageSize),
                factoryId,
                number,
                styleNumber,
                categoryId,
                startDate,
                endDate,
                flag
        );

        return Result.ok(page);
    }

    /**
     * 新增工单。
     *
     * 新增前会校验提交数据在所选日期当天是否存在重复记录，若存在则拒绝保存。
     *
     * @param workOrder 待保存的工单数据
     * @return 保存结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> saveWorkOrder(JobWorkOrder workOrder) {
        if (hasDuplicateWorkOrderInSameDay(workOrder, null)) {
            throw new BizException("所选日期当天已存在相同工单数据，不允许重复提交");
        }

        // 床是 (厂, 床号, 款式) 的唯一记录，工单只挂 batchId，不再各自抄一份
        Batch batch = batchService.findOrCreate(workOrder.getFactoryId(), workOrder.getNumber(), workOrder.getStyleNumber());
        workOrder.setBatchId(batch.getId());

        if (!save(workOrder)) {
            throw new BizException("新增工单失败，请重试");
        }
        return Result.ok();
    }

    /**
     * 修改工单。
     *
     * 修改前会校验提交数据在所选日期当天是否存在重复记录，校验时会排除当前编辑记录本身。
     *
     * @param workOrder 待更新的工单数据
     * @return 更新结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> updateWorkOrder(JobWorkOrder workOrder) {
        if (hasDuplicateWorkOrderInSameDay(workOrder, workOrder.getId())) {
            throw new BizException("所选日期当天已存在相同工单数据，不允许重复提交");
        }

        JobWorkOrder original = getById(workOrder.getId());
        if (original == null) {
            throw new BizException("修改工单失败：该工单不存在或已被删除");
        }

        // 数量不能被改到「已经认领的量」以下：
        // 一床布压花、刮胶各领一遍，用的是同一个预设数量，所以只要任一个工种已经领了，
        // 数量就不能小于它，否则那部分认领记录就凭空超出工单了。
        Integer newQuantity = workOrder.getQuantity();
        if (ObjectUtil.isNotNull(newQuantity) && !newQuantity.equals(original.getQuantity())) {
            Integer maxClaimed = jobMapper.maxClaimedQuantityByMode(workOrder.getId());
            int claimed = maxClaimed == null ? 0 : maxClaimed;
            if (newQuantity < claimed) {
                throw new BizException("该工单已被认领 " + claimed + " 件，数量不能小于 " + claimed
                        + "；如需下调，请先撤销多出来的认领记录");
            }
        }

        // 床号/款式/厂改了，先改床本身，由它把关联的工单、账单、工作记录一起刷新；
        // 没挂床的老数据（迁移前的）这里补挂一张
        Integer batchId = original.getBatchId();
        if (batchId == null) {
            Batch batch = batchService.findOrCreate(workOrder.getFactoryId(), workOrder.getNumber(), workOrder.getStyleNumber());
            batchId = batch.getId();
        } else {
            batchService.renameBatch(batchId, workOrder.getFactoryId(), workOrder.getNumber(), workOrder.getStyleNumber());
        }
        workOrder.setBatchId(batchId);

        if (!updateById(workOrder)) {
            throw new BizException("修改工单失败：该工单不存在，或已被其他人修改，请刷新后重试");
        }
        return Result.ok();
    }

    /**
     * 校验工单是否在目标日期当天存在重复数据。
     *
     * 判重维度：成衣厂ID、床号、款式编号、工作类型、数量。
     * 编辑场景会排除当前记录本身。
     */
    private boolean hasDuplicateWorkOrderInSameDay(JobWorkOrder workOrder, Integer excludeId) {
        LocalDate targetDate = ObjectUtil.defaultIfNull(workOrder.getCreatedDate(), LocalDate.now());

        Long duplicateCount = lambdaQuery()
                .eq(JobWorkOrder::getFactoryId, workOrder.getFactoryId())
                .eq(JobWorkOrder::getNumber, workOrder.getNumber())
                .eq(JobWorkOrder::getStyleNumber, workOrder.getStyleNumber())
                .eq(JobWorkOrder::getCategoryId, workOrder.getCategoryId())
                .eq(JobWorkOrder::getQuantity, workOrder.getQuantity())
                // 仅在正常状态（未逻辑删除）的数据中判重
                .eq(JobWorkOrder::getFlag, 0)
                .eq(JobWorkOrder::getCreatedDate, targetDate)
                .ne(ObjectUtil.isNotEmpty(excludeId), JobWorkOrder::getId, excludeId)
                .count();

        return duplicateCount > 0;
    }

    /**
     * 删除工单。
     *
     * @param id 工单主键ID
     * @return 删除结果
     */
    /**
     * 删除工单。
     *
     * <p>已经有人认领的工单不允许直接删：认领记录仍然指向它，删掉之后
     * 「这床活是从哪张工单派下来的」就断了，工资也再核对不了。
     * 要删就先撤销认领（删掉对应的工作记录）。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> deleteWorkOrder(Integer id) {
        if (ObjectUtil.isNull(id)) {
            throw new BizException("工单ID不能为空");
        }

        Long claimedCount = jobMapper.selectCount(
                Wrappers.<Job>lambdaQuery()
                        .eq(Job::getWorkOrderId, id)
                        .eq(Job::getFlag, 0));
        if (claimedCount != null && claimedCount > 0) {
            throw new BizException("该工单已有 " + claimedCount + " 条认领记录，不能直接删除；请先撤销认领");
        }

        if (!removeById(id)) {
            throw new BizException("删除工单失败：该工单不存在或已被删除");
        }
        return Result.ok();
    }

    /**
     * 查询工单在指定工作方式下的认领进度。
     *
     * <p>给认领弹窗用：员工在填数量之前就能看到「还剩多少」，
     * 不用先提交一个大数字、被系统驳回之后再改小。</p>
     */
    @Override
    @Transactional(readOnly = true)
    public Result<Object> queryWorkOrderProgress(Integer workOrderId, Integer employeeId, Integer modeId) {
        if (ObjectUtil.isNull(workOrderId)) {
            throw new BizException("工单ID不能为空");
        }

        JobWorkOrder workOrder = lambdaQuery()
                .eq(JobWorkOrder::getId, workOrderId)
                .eq(JobWorkOrder::getFlag, 0)
                .one();
        if (ObjectUtil.isNull(workOrder)) {
            throw new BizException("工单不存在或已被删除");
        }

        Integer queryModeId = modeId;
        if (ObjectUtil.isNull(queryModeId) && ObjectUtil.isNotNull(employeeId)) {
            Employee employee = employeeService.lambdaQuery()
                    .eq(Employee::getId, employeeId)
                    .eq(Employee::getFlag, 0)
                    .one();
            if (ObjectUtil.isNull(employee)) {
                throw new BizException("员工不存在或已被删除");
            }
            queryModeId = employee.getModeId();
        }

        int claimed = 0;
        if (ObjectUtil.isNotNull(queryModeId)) {
            Integer sum = jobMapper.sumClaimedQuantityByMode(workOrderId, queryModeId);
            claimed = sum == null ? 0 : sum;
        }

        int total = ObjectUtil.defaultIfNull(workOrder.getQuantity(), 0);
        JobWorkOrderProgressDTO progress = new JobWorkOrderProgressDTO();
        progress.setWorkOrderId(workOrderId);
        progress.setQuantity(total);
        progress.setModeId(queryModeId);
        progress.setClaimedQuantity(claimed);
        progress.setRemainQuantity(Math.max(total - claimed, 0));
        return Result.ok(progress);
    }
}
