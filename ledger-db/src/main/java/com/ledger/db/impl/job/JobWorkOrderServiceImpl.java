package com.ledger.db.impl.job;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ledger.common.result.Result;
import com.ledger.db.entity.JobWorkOrder;
import com.ledger.db.entity.dto.JobWorkOrderDTO;
import com.ledger.db.mapper.JobWorkOrderMapper;
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
public class JobWorkOrderServiceImpl extends ServiceImpl<JobWorkOrderMapper, JobWorkOrder> implements IJobWorkOrderService {

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
        Page<JobWorkOrderDTO> page = baseMapper.selectWorkOrderListByCondition(
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
     * 新增前会校验提交数据在前后20天内是否存在重复记录，若存在则拒绝保存。
     *
     * @param workOrder 待保存的工单数据
     * @return 保存结果
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Object> saveWorkOrder(JobWorkOrder workOrder) {
        if (hasDuplicateWorkOrderIn20Days(workOrder, null)) {
            return Result.fail("前后20天内已存在相同工单数据，不允许重复提交");
        }

        if (save(workOrder)) {
            return Result.ok();
        }
        return Result.fail();
    }

    /**
     * 修改工单。
     *
     * 修改前会校验提交数据在前后20天内是否存在重复记录，校验时会排除当前编辑记录本身。
     *
     * @param workOrder 待更新的工单数据
     * @return 更新结果
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Object> updateWorkOrder(JobWorkOrder workOrder) {
        if (hasDuplicateWorkOrderIn20Days(workOrder, workOrder.getId())) {
            return Result.fail("前后20天内已存在相同工单数据，不允许重复提交");
        }

        if (updateById(workOrder)) {
            return Result.ok();
        }
        return Result.fail();
    }

    /**
     * 校验工单是否在目标日期前后20天内存在重复数据。
     *
     * 判重维度：成衣厂ID、床号、款式编号、工作类型、数量。
     * 编辑场景会排除当前记录本身。
     */
    private boolean hasDuplicateWorkOrderIn20Days(JobWorkOrder workOrder, Integer excludeId) {
        LocalDate targetDate = ObjectUtil.defaultIfNull(workOrder.getCreatedDate(), LocalDate.now());
        LocalDate startDate = targetDate.minusDays(20);
        LocalDate endDate = targetDate.plusDays(20);

        Long duplicateCount = lambdaQuery()
                .eq(JobWorkOrder::getFactoryId, workOrder.getFactoryId())
                .eq(JobWorkOrder::getNumber, workOrder.getNumber())
                .eq(JobWorkOrder::getStyleNumber, workOrder.getStyleNumber())
                .eq(JobWorkOrder::getCategoryId, workOrder.getCategoryId())
                .eq(JobWorkOrder::getQuantity, workOrder.getQuantity())
                .ge(JobWorkOrder::getCreatedDate, startDate)
                .le(JobWorkOrder::getCreatedDate, endDate)
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
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Object> deleteWorkOrder(Integer id) {
        if (removeById(id)) {
            return Result.ok();
        }
        return Result.fail();
    }
}
