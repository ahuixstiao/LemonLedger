package com.ledger.db.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.common.exception.BizException;
import com.ledger.common.result.Result;
import com.ledger.db.entity.Batch;
import com.ledger.db.entity.FactoryBill;
import com.ledger.db.entity.Job;
import com.ledger.db.entity.JobWorkOrder;
import com.ledger.db.mapper.BatchMapper;
import com.ledger.db.mapper.FactoryBillMapper;
import com.ledger.db.mapper.JobMapper;
import com.ledger.db.mapper.JobWorkOrderMapper;
import com.ledger.db.service.IBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 床 服务实现类
 * </p>
 *
 * @author ahui
 * @since 2026-11-06
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BatchServiceImpl extends LedgerServiceImpl<BatchMapper, Batch> implements IBatchService {

    private final BatchMapper batchMapper;

    private final JobMapper jobMapper;

    private final JobWorkOrderMapper jobWorkOrderMapper;

    private final FactoryBillMapper factoryBillMapper;

    /**
     * 按 (厂, 床号, 款式) 找床，没有就建一张。
     * <p>
     * 并发下两个人同时开同一张床的单据时，后手会撞唯一键，
     * 这里接住 DuplicateKeyException 再查一次，拿到先手建好的那条。
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Batch findOrCreate(Integer factoryId, String number, String styleNumber) {

        number = trimToEmpty(number);
        styleNumber = trimToEmpty(styleNumber);

        Batch exists = findBatch(factoryId, number, styleNumber);
        if (exists != null) {
            return exists;
        }

        Batch batch = new Batch();
        batch.setFactoryId(factoryId);
        batch.setNumber(number);
        batch.setStyleNumber(styleNumber);

        try {
            if (!save(batch)) {
                throw new BizException("新建床失败，请重试");
            }
            return batch;
        } catch (DuplicateKeyException exception) {
            Batch created = findBatch(factoryId, number, styleNumber);
            if (created == null) {
                throw new BizException("新建床失败：同一张床正在被另一处录入，请重试", exception);
            }
            return created;
        }
    }

    /**
     * 改床号/款式，同时刷新工单、账单、工作记录上的冗余列。
     *
     * <p>目标 (厂, 床号, 款式) 已经被另一张床占着时，不再报错：直接复用那张床，
     * 让调用方把单据挪过去。这样「编辑账单/工单时换了个款式」不会被卡住，
     * 也不会把原来那张床上的其它单据一起改掉。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Batch renameBatch(Integer batchId, Integer factoryId, String number, String styleNumber) {

        number = trimToEmpty(number);
        styleNumber = trimToEmpty(styleNumber);

        Batch sameBatch = findBatch(factoryId, number, styleNumber);
        if (sameBatch != null) {
            // 已经是自己，或者同号同款的另一张床：两种情况都直接用它
            return sameBatch;
        }

        Batch batch = getById(batchId);
        if (batch == null) {
            // 床被删了（历史数据），按目标信息补一张
            return findOrCreate(factoryId, number, styleNumber);
        }

        batch.setFactoryId(factoryId);
        batch.setNumber(number);
        batch.setStyleNumber(styleNumber);
        if (!updateById(batch)) {
            throw new BizException("修改床失败：这张床已被其他人修改，请刷新后重试");
        }

        batchMapper.updateJobBatchInfo(batchId, factoryId, number, styleNumber);
        batchMapper.updateWorkOrderBatchInfo(batchId, factoryId, number, styleNumber);
        batchMapper.updateBillBatchInfo(batchId, factoryId, number);
        return batch;
    }

    /**
     * 分页查询床列表
     */
    @Override
    @Transactional(readOnly = true)
    public Result<Object> queryBatchList(Integer factoryId, String number, String styleNumber,
                                         Integer currentPage, Integer pageSize) {

        Page<Batch> page = new Page<>(currentPage, pageSize);
        page = batchMapper.selectPage(page, Wrappers.<Batch>lambdaQuery()
                .eq(factoryId != null, Batch::getFactoryId, factoryId)
                .like(StringUtils.hasText(number), Batch::getNumber, number)
                .like(StringUtils.hasText(styleNumber), Batch::getStyleNumber, styleNumber)
                .orderByDesc(Batch::getId));

        return Result.ok(page);
    }

    /**
     * 删除床（逻辑删除），被单据引用时拒绝
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> deleteBatch(Integer batchId) {

        long billCount = factoryBillMapper.selectCount(
                Wrappers.<FactoryBill>lambdaQuery().eq(FactoryBill::getBatchId, batchId).eq(FactoryBill::getFlag, 0));
        long jobCount = jobMapper.selectCount(
                Wrappers.<Job>lambdaQuery().eq(Job::getBatchId, batchId).eq(Job::getFlag, 0));
        long workOrderCount = jobWorkOrderMapper.selectCount(
                Wrappers.<JobWorkOrder>lambdaQuery().eq(JobWorkOrder::getBatchId, batchId).eq(JobWorkOrder::getFlag, 0));

        if (billCount > 0 || jobCount > 0 || workOrderCount > 0) {
            throw new BizException("这张床还有 " + billCount + " 条账单、" + workOrderCount + " 张工单、"
                    + jobCount + " 条工作记录，不能直接删除");
        }

        if (!removeById(batchId)) {
            throw new BizException("删除床失败：这张床不存在或已被删除");
        }

        return Result.ok();
    }

    /**
     * 床上一张单据都不剩时，把它一起逻辑删除，避免床列表里堆积空床
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteIfEmpty(Integer batchId) {

        if (batchId == null) {
            return;
        }

        long billCount = factoryBillMapper.selectCount(
                Wrappers.<FactoryBill>lambdaQuery().eq(FactoryBill::getBatchId, batchId));
        long jobCount = jobMapper.selectCount(
                Wrappers.<Job>lambdaQuery().eq(Job::getBatchId, batchId));
        long workOrderCount = jobWorkOrderMapper.selectCount(
                Wrappers.<JobWorkOrder>lambdaQuery().eq(JobWorkOrder::getBatchId, batchId));

        if (billCount == 0 && jobCount == 0 && workOrderCount == 0) {
            removeById(batchId);
        }
    }

    /**
     * 查 (厂, 床号, 款式) 对应的床；床号前后空格统一去掉再比
     */
    private Batch findBatch(Integer factoryId, String number, String styleNumber) {
        return getOne(Wrappers.<Batch>lambdaQuery()
                .eq(Batch::getFactoryId, factoryId)
                .eq(Batch::getNumber, number)
                .eq(Batch::getStyleNumber, styleNumber)
                .last("limit 1"), false);
    }

    /**
     * 空值统一成空串：batch.style_number 是 NOT NULL（唯一键里 NULL 不算重复），
     * 前端没填款式时传过来的是 null，直接写库会撞类型错误
     */
    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
