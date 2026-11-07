package com.ledger.db.impl.factory;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.common.exception.BizException;
import com.ledger.common.result.Result;
import com.ledger.db.entity.Factory;
import com.ledger.db.entity.FactoryBill;
import com.ledger.db.entity.FactoryPieceRate;
import com.ledger.db.entity.Job;
import com.ledger.db.entity.JobWorkOrder;
import com.ledger.db.impl.LedgerServiceImpl;
import com.ledger.db.mapper.FactoryBillMapper;
import com.ledger.db.mapper.FactoryMapper;
import com.ledger.db.mapper.FactoryPieceRateMapper;
import com.ledger.db.mapper.JobMapper;
import com.ledger.db.mapper.JobWorkOrderMapper;
import com.ledger.db.service.factory.IFactoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 成衣厂表 服务实现类
 * </p>
 *
 * @author ahui
 * @since 2025-10-03
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FactoryServiceImpl extends LedgerServiceImpl<FactoryMapper, Factory> implements IFactoryService {

    private final FactoryBillMapper factoryBillMapper;

    private final FactoryPieceRateMapper factoryPieceRateMapper;

    private final JobMapper jobMapper;

    private final JobWorkOrderMapper jobWorkOrderMapper;

    /**
     * 查询成衣厂列表
     *
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @return result
     */
    @Override
    public Result<Object> queryFactoryList(String factoryName, Integer currentPage, Integer pageSize, Integer flag) {

        // 构建分页对象
        Page<Factory> page = new Page<>(currentPage, pageSize);

        String keyword = StrUtil.trim(factoryName);
        Integer queryFlag = flag == null ? 0 : flag;

        return Result.ok(baseMapper.queryFactoryList(page, keyword, queryFlag));
    }

    /**
     * 保存成衣厂信息
     *
     * @param factory 成衣厂实体
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> saveFactoryInfo(Factory factory) {

        if (!save(factory)) {
            throw new BizException("新增成衣厂失败，请重试");
        }

        return Result.ok();
    }

    /**
     * 更新成衣厂信息
     *
     * @param factory 成衣厂实体
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> updateFactory(Factory factory) {
        if (factory.getId() == null) {
            throw new BizException("工厂ID不能为空");
        }

        if (!updateById(factory)) {
            throw new BizException("修改成衣厂失败：该成衣厂不存在，或已被其他人修改，请刷新后重试");
        }

        return Result.ok();
    }

    /**
     * 删除成衣厂信息（逻辑删除）
     *
     * @param id 成衣厂ID
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> deleteFactory(Integer id) {
        if (id == null) {
            throw new BizException("工厂ID不能为空");
        }

        // 删除前拦住"删了厂、账单和工单还在"的情况：
        // 那些单据里的 factory_id 会指向一个已删除的厂，列表里厂名变成空白，
        // 账单统计和工资核对都再也对不上，而且逻辑删除让外键约束根本不会触发。
        long billCount = factoryBillMapper.selectCount(
                Wrappers.<FactoryBill>lambdaQuery().eq(FactoryBill::getFactoryId, id).eq(FactoryBill::getFlag, 0));
        long priceCount = factoryPieceRateMapper.selectCount(
                Wrappers.<FactoryPieceRate>lambdaQuery().eq(FactoryPieceRate::getFactoryId, id).eq(FactoryPieceRate::getFlag, 0));
        long workOrderCount = jobWorkOrderMapper.selectCount(
                Wrappers.<JobWorkOrder>lambdaQuery().eq(JobWorkOrder::getFactoryId, id).eq(JobWorkOrder::getFlag, 0));
        long jobCount = jobMapper.selectCount(
                Wrappers.<Job>lambdaQuery().eq(Job::getFactoryId, id).eq(Job::getFlag, 0));

        if (billCount > 0 || priceCount > 0 || workOrderCount > 0 || jobCount > 0) {
            throw new BizException("该成衣厂还有 " + billCount + " 条账单、"
                    + priceCount + " 条计件单价、" + workOrderCount + " 张工单、"
                    + jobCount + " 条工作记录，不能直接删除。请先处理这些单据");
        }

        if (!removeById(id)) {
            throw new BizException("删除成衣厂失败：该成衣厂不存在或已被删除");
        }

        return Result.ok();
    }
}
