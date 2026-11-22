package com.ledger.db.impl.settlement;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.common.exception.BizException;
import com.ledger.common.result.Result;
import com.ledger.db.entity.FactoryBillSettlement;
import com.ledger.db.entity.FactoryBillSettlementItem;
import com.ledger.db.entity.dto.FactoryBillDto;
import com.ledger.db.mapper.FactoryBillSettlementItemMapper;
import com.ledger.db.mapper.FactoryBillSettlementMapper;
import com.ledger.db.service.settlement.IFactoryBillSettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 成衣厂账单结算单实现。
 *
 * @author ahui
 * @since 2026-11-22
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class FactoryBillSettlementServiceImpl implements IFactoryBillSettlementService {

    private final FactoryBillSettlementMapper settlementMapper;
    private final FactoryBillSettlementItemMapper itemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FactoryBillSettlement snapshotOnExport(Integer factoryId,
                                                  String factoryName,
                                                  LocalDate startDate,
                                                  LocalDate endDate,
                                                  List<FactoryBillDto> rows) {
        if (factoryId == null) {
            return null;
        }
        List<FactoryBillDto> bills = rows == null ? new ArrayList<>() : rows;

        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQuantity = 0;
        for (FactoryBillDto bill : bills) {
            totalAmount = totalAmount.add(Objects.requireNonNullElse(bill.getBill(), BigDecimal.ZERO));
            totalQuantity += Objects.requireNonNullElse(bill.getQuantity(), 0);
        }

        // 同一区间还没结清的那条，直接把金额和明细刷新掉，免得每点一次导出就多一条
        FactoryBillSettlement settlement = settlementMapper.selectOne(new LambdaQueryWrapper<FactoryBillSettlement>()
                .eq(FactoryBillSettlement::getFactoryId, factoryId)
                .eq(FactoryBillSettlement::getFlag, 0)
                .ne(FactoryBillSettlement::getStatus, FactoryBillSettlement.STATUS_PAID)
                .eq(startDate != null, FactoryBillSettlement::getStartDate, startDate)
                .isNull(startDate == null, FactoryBillSettlement::getStartDate)
                .eq(endDate != null, FactoryBillSettlement::getEndDate, endDate)
                .isNull(endDate == null, FactoryBillSettlement::getEndDate)
                .orderByDesc(FactoryBillSettlement::getId)
                .last("limit 1"));

        boolean create = settlement == null;
        if (create) {
            settlement = new FactoryBillSettlement();
            settlement.setFactoryId(factoryId);
            settlement.setPaidAmount(BigDecimal.ZERO);
            settlement.setStatus(FactoryBillSettlement.STATUS_UNPAID);
            settlement.setFlag(0);
            settlement.setCreatedDate(LocalDate.now());
        }

        settlement.setFactoryName(factoryName);
        settlement.setStartDate(startDate);
        settlement.setEndDate(endDate);
        settlement.setBillCount(bills.size());
        settlement.setTotalQuantity(totalQuantity);
        settlement.setTotalAmount(totalAmount);

        if (create) {
            settlementMapper.insert(settlement);
        } else {
            settlementMapper.updateById(settlement);
            // 明细整批重建：区间内删掉的床不应该还留在快照里
            itemMapper.delete(new LambdaQueryWrapper<FactoryBillSettlementItem>()
                    .eq(FactoryBillSettlementItem::getSettlementId, settlement.getId()));
        }

        for (FactoryBillDto bill : bills) {
            FactoryBillSettlementItem item = new FactoryBillSettlementItem();
            item.setSettlementId(settlement.getId().longValue());
            item.setBillId(bill.getId() == null ? null : bill.getId().longValue());
            item.setNumber(bill.getNumber());
            item.setStyleNumber(bill.getStyleNumber());
            item.setCategory(bill.getCategory());
            item.setQuantity(Objects.requireNonNullElse(bill.getQuantity(), 0));
            item.setPrice(bill.getPrice());
            item.setAmount(bill.getBill());
            item.setFlag(0);
            item.setCreatedDate(LocalDate.now());
            itemMapper.insert(item);
        }

        return settlement;
    }

    @Override
    public Result<Object> querySettlementList(Integer factoryId, Integer status, String startDate, String endDate,
                                              Integer currentPage, Integer pageSize) {
        Page<FactoryBillSettlement> page = new Page<>(
                currentPage == null || currentPage < 1 ? 1 : currentPage,
                pageSize == null || pageSize < 1 ? 10 : pageSize);

        LambdaQueryWrapper<FactoryBillSettlement> wrapper = new LambdaQueryWrapper<FactoryBillSettlement>()
                .eq(FactoryBillSettlement::getFlag, 0)
                .eq(factoryId != null, FactoryBillSettlement::getFactoryId, factoryId)
                .eq(status != null, FactoryBillSettlement::getStatus, status)
                .ge(hasText(startDate), FactoryBillSettlement::getCreatedDate, startDate)
                .le(hasText(endDate), FactoryBillSettlement::getCreatedDate, endDate)
                .orderByDesc(FactoryBillSettlement::getId);

        return Result.ok(settlementMapper.selectPage(page, wrapper));
    }

    @Override
    public Result<Object> querySettlementItems(Integer id) {
        if (id == null) {
            throw new BizException("结算单ID不能为空");
        }
        List<FactoryBillSettlementItem> items = itemMapper.selectList(new LambdaQueryWrapper<FactoryBillSettlementItem>()
                .eq(FactoryBillSettlementItem::getSettlementId, id.longValue())
                .eq(FactoryBillSettlementItem::getFlag, 0)
                .orderByAsc(FactoryBillSettlementItem::getId));
        return Result.ok(items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> settle(Integer id, BigDecimal paidAmount, LocalDate paidDate, String remark) {
        FactoryBillSettlement settlement = settlementMapper.selectById(id);
        if (settlement == null || Objects.equals(settlement.getFlag(), 1)) {
            throw new BizException("结算单不存在或已作废");
        }

        BigDecimal paid = paidAmount == null ? settlement.getPaidAmount() : paidAmount;
        if (paid == null) {
            paid = BigDecimal.ZERO;
        }
        if (paid.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException("已付金额不能是负数");
        }
        BigDecimal total = Objects.requireNonNullElse(settlement.getTotalAmount(), BigDecimal.ZERO);
        if (paid.compareTo(total) > 0) {
            throw new BizException("已付金额不能超过账单总额 " + total.toPlainString());
        }

        settlement.setPaidAmount(paid);
        if (paidDate != null) {
            settlement.setPaidDate(paidDate);
        }
        if (hasText(remark)) {
            settlement.setRemark(remark);
        }
        settlement.setStatus(paid.compareTo(BigDecimal.ZERO) <= 0
                ? FactoryBillSettlement.STATUS_UNPAID
                : (paid.compareTo(total) >= 0 ? FactoryBillSettlement.STATUS_PAID : FactoryBillSettlement.STATUS_PARTIAL));
        settlementMapper.updateById(settlement);
        return Result.ok(settlement);
    }

    @Override
    public Result<Object> voidSettlement(Integer id) {
        FactoryBillSettlement settlement = settlementMapper.selectById(id);
        if (settlement == null || Objects.equals(settlement.getFlag(), 1)) {
            throw new BizException("结算单不存在或已作废");
        }
        // @TableLogic 的逻辑删除字段不会被 updateById 写进 SET，必须走 deleteById 才会真正置 flag=1
        settlementMapper.deleteById(id);
        itemMapper.delete(new LambdaQueryWrapper<FactoryBillSettlementItem>()
                .eq(FactoryBillSettlementItem::getSettlementId, id.longValue()));
        return Result.ok(null);
    }

    @Override
    public FactoryBillSettlement getSettlement(Integer id) {
        FactoryBillSettlement settlement = settlementMapper.selectById(id);
        if (settlement == null || Objects.equals(settlement.getFlag(), 1)) {
            return null;
        }
        return settlement;
    }

    @Override
    public List<FactoryBillSettlementItem> getSettlementItems(Integer id) {
        return itemMapper.selectList(new LambdaQueryWrapper<FactoryBillSettlementItem>()
                .eq(FactoryBillSettlementItem::getSettlementId, id.longValue())
                .eq(FactoryBillSettlementItem::getFlag, 0)
                .orderByAsc(FactoryBillSettlementItem::getId));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
