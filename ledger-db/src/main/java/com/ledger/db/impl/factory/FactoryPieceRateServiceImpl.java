package com.ledger.db.impl.factory;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.common.exception.BizException;
import com.ledger.common.result.Result;
import com.ledger.db.entity.FactoryPieceRate;
import com.ledger.db.entity.FactoryStyle;
import com.ledger.db.entity.dto.FactoryPieceRateDto;
import com.ledger.db.impl.LedgerServiceImpl;
import com.ledger.db.mapper.FactoryBillMapper;
import com.ledger.db.mapper.FactoryPieceRateMapper;
import com.ledger.db.service.factory.IFactoryStyleService;
import com.ledger.db.service.factory.IFactoryPieceRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * <p>
 * 成衣厂计件单价表 服务实现类
 * </p>
 *
 * @author ahui
 * @since 2025-10-16
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FactoryPieceRateServiceImpl extends LedgerServiceImpl<FactoryPieceRateMapper, FactoryPieceRate> implements IFactoryPieceRateService {

    private final FactoryPieceRateMapper factoryPieceRateMapper;

    private final FactoryBillMapper factoryBillMapper;

    private final IFactoryStyleService factoryStyleService;

    /**
     * 查询成衣厂计件单价列表 如果有条件则按条件查询
     *
     * @param factoryId   工厂ID
     * @param styleNumber 款式编号
     * @param categoryId  工作类型ID
     * @param currentPage 当前页
     * @param pageSize    页面条数
     * @param flag        状态
     * @return result
     */
    @Override
    @Transactional(readOnly = true)
    public Result<Object> queryFactoryPieceRateListByCondition(
            Integer factoryId, String styleNumber, Integer categoryId,
            Integer currentPage, Integer pageSize, Integer flag) {

        // 构建分页对象
        Page<FactoryPieceRateDto> page = new Page<>(currentPage, pageSize);

        page = factoryPieceRateMapper.selectFactoryPieceRateListByCondition(page, factoryId, styleNumber, categoryId, flag);

        return Result.ok(page);
    }

    /**
     * 账单用：查询某成衣厂的计件单价下拉项
     *
     * @param factoryId 成衣厂ID
     * @param flag      删除状态 0否 1是
     * @return result
     */
    @Override
    @Transactional(readOnly = true)
    public Result<Object> queryPieceRateOptionsByFactoryId(Integer factoryId, Integer flag) {
        return Result.ok(factoryPieceRateMapper.selectPieceRateOptionsByFactoryId(factoryId, flag));
    }

    /**
     * 保存成衣厂计件单价信息：同一款式 + 同一工作类型只能有一条
     *
     * @param factoryPieceRate 成衣厂计件单价实体
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> saveFactoryPieceRateInfo(FactoryPieceRate factoryPieceRate) {
        if (ObjectUtil.isEmpty(factoryPieceRate.getStyleId())) {
            throw new BizException("请先选择款式");
        }
        if (ObjectUtil.isEmpty(factoryPieceRate.getCategoryId())) {
            throw new BizException("请选择工作类型");
        }
        if (ObjectUtil.isEmpty(factoryPieceRate.getPrice())) {
            throw new BizException("请填写计件单价");
        }

        // 成衣厂取自款式本身，避免前端传错厂
        FactoryStyle style = factoryStyleService.getById(factoryPieceRate.getStyleId());
        if (ObjectUtil.isEmpty(style)) {
            throw new BizException("款式不存在，请刷新后重试");
        }
        factoryPieceRate.setFactoryId(style.getFactoryId());

        boolean exists = lambdaQuery()
                .eq(FactoryPieceRate::getStyleId, factoryPieceRate.getStyleId())
                .eq(FactoryPieceRate::getCategoryId, factoryPieceRate.getCategoryId())
                .exists();
        if (exists) {
            throw new BizException("计件单价信息已存在：同一款式 + 同一工作类型只能有一条计件单价");
        }

        if (!save(factoryPieceRate)) {
            throw new BizException("新增计件单价失败，请重试");
        }
        return Result.ok();
    }

    /**
     * 更新成衣厂计件单价信息；单价有变化时按新单价重算指定日期区间内的账单金额。
     *
     * @param factoryPieceRate 成衣厂计件单价实体
     * @param startDate        受影响账单的开始日期（改单价时必填）
     * @param endDate          受影响账单的结束日期（改单价时必填）
     * @return result，data 里带同步的账单条数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> updateFactoryPieceRateInfo(FactoryPieceRate factoryPieceRate, String startDate, String endDate) {

        FactoryPieceRate original = ObjectUtil.isEmpty(factoryPieceRate.getId())
                ? null
                : getById(factoryPieceRate.getId());
        if (ObjectUtil.isEmpty(original)) {
            throw new BizException("修改计件单价失败：该计件单价不存在，请刷新后重试");
        }

        // 只有单价真的变了才需要重算账单；改款式/工种不触发
        boolean priceChanged = ObjectUtil.isNotEmpty(factoryPieceRate.getPrice())
                && (ObjectUtil.isEmpty(original.getPrice())
                || original.getPrice().compareTo(factoryPieceRate.getPrice()) != 0);

        int affectedBills = 0;
        if (priceChanged) {
            if (StrUtil.isBlank(startDate) || StrUtil.isBlank(endDate)) {
                throw new BizException("改单价时要先选择受影响的账单日期范围");
            }
            LocalDate syncStart = parseSyncDate(startDate, "受影响账单开始日期");
            LocalDate syncEnd = parseSyncDate(endDate, "受影响账单结束日期");
            if (syncStart.isAfter(syncEnd)) {
                throw new BizException("受影响账单的开始日期不能晚于结束日期");
            }
            affectedBills = factoryBillMapper.updateBillAmountByPieceRate(
                    factoryPieceRate.getId(),
                    factoryPieceRate.getPrice(),
                    syncStart.toString(),
                    syncEnd.toString());
        }

        // 换款式时成衣厂跟着款式走
        if (ObjectUtil.isNotEmpty(factoryPieceRate.getStyleId())) {
            FactoryStyle style = factoryStyleService.getById(factoryPieceRate.getStyleId());
            if (ObjectUtil.isEmpty(style)) {
                throw new BizException("款式不存在，请刷新后重试");
            }
            factoryPieceRate.setFactoryId(style.getFactoryId());
        }

        if (!updateById(factoryPieceRate)) {
            throw new BizException("修改计件单价失败：该计件单价不存在，或已被其他人修改，请刷新后重试");
        }

        if (priceChanged) {
            return Result.ok("单价已更新，同步重算 " + affectedBills + " 张账单",
                    Map.of("affectedBills", affectedBills));
        }
        return Result.ok();
    }

    /**
     * 解析同步账单用的日期，格式不对时给出明确提示。
     *
     * @param value 日期文本
     * @param label 字段名，用于拼错误提示
     * @return 解析后的日期
     */
    private LocalDate parseSyncDate(String value, String label) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new BizException(label + "格式不对，应为 yyyy-MM-dd");
        }
    }

    /**
     * 删除成衣厂计件单价信息
     *
     * @param id 成衣厂计件单价ID
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> deleteFactoryPieceRateById(Integer id) {
        if (!removeById(id)) {
            throw new BizException("删除计件单价失败：该计件单价不存在或已被删除");
        }
        return Result.ok();
    }
}
