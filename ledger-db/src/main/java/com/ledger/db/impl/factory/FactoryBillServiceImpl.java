package com.ledger.db.impl.factory;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.common.exception.BizException;
import com.ledger.common.result.Result;
import com.ledger.common.util.MoneyUtil;
import com.ledger.db.entity.Batch;
import com.ledger.db.entity.FactoryBill;
import com.ledger.db.impl.LedgerServiceImpl;
import com.ledger.db.util.BillDuplicateCheckUtil;
import com.ledger.db.util.FactoryBillExportSortUtil;
import com.ledger.db.entity.FactoryPieceRate;
import com.ledger.db.entity.dto.FactoryBillDto;
import com.ledger.db.mapper.FactoryBillMapper;
import com.ledger.db.service.IBatchService;
import com.ledger.db.service.factory.IFactoryBillService;
import com.ledger.db.service.factory.IFactoryPieceRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 成衣厂账单表 服务实现类
 * </p>
 *
 * @author ahui
 * @since 2025-10-15
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FactoryBillServiceImpl extends LedgerServiceImpl<FactoryBillMapper, FactoryBill> implements IFactoryBillService {

    private final FactoryBillMapper factoryBillMapper;

    private final IFactoryPieceRateService factoryPieceRateService;

    private final IBatchService batchService;

    /**
     * 账单查重的日期窗口（天），来自配置 ledger.bill.duplicate-window-days。
     * 原来是写死的 20 天，返工/补做时没有放行手段。
     */
    @Value("${ledger.bill.duplicate-window-days:20}")
    private int duplicateWindowDays;

    /**
     * 按条件查询成衣厂账单列表（款式来源于计件单价表）
     *
     * @param factoryId   工厂ID
     * @param number      床号
     * @param styleNumber 款式编号（计件单价表）
     * @param flag        删除状态 0否 1是
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @param currentPage 当前页
     * @param pageSize    页面条数
     * @return result
     */
    @Override
    @Transactional(readOnly = true)
    public Result<Object> queryFactoryBillListByCondition(Integer factoryId, String number, String styleNumber, Integer flag, String startDate, String endDate, Integer currentPage, Integer pageSize) {

        // 规整日期参数：过滤空串/"0"/非法日期，避免传入 SQL 导致 Incorrect DATE value
        startDate = normalizeDateParam(startDate);
        endDate = normalizeDateParam(endDate);

        // 配置分页参数
        Page<FactoryBillDto> page = new Page<>(currentPage, pageSize);

        page = factoryBillMapper.selectFactoryBillList(
                page,
                factoryId, number, styleNumber, flag,
                startDate, endDate);

        // 空结果返回空分页，而不是 fail。
        // 查不到数据是正常情况：返回 fail 会让前端把它当出错处理（列表清空、可能弹错误提示），
        // 而且「翻到第 2 页恰好没有数据」与「真的查询失败」在调用方看来一模一样。
        return Result.ok(page);
    }

    /**
     * 统计成衣厂账单
     *
     * @param factoryId 成衣厂ID
     * @param startDate 起止日期
     * @param endDate   起止日期
     * @param flag      状态
     * @return result
     */
    @Override
    public Result<Object> statisticalBill(Integer factoryId, String startDate, String endDate, Integer flag) {
        startDate = normalizeDateParam(startDate);
        endDate = normalizeDateParam(endDate);

        // 统计成衣厂账单
        FactoryBillDto factoryBillDto = factoryBillMapper.calculateBillByFactoryIdAndDate(factoryId, startDate, endDate, flag);

        if (ObjectUtil.isNotEmpty(factoryBillDto)) {
            return Result.ok(factoryBillDto);
        }

        return Result.fail("暂无账单");
    }

    /**
     * 保存账单信息
     *
     * @param bill 账单实体
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> saveFactoryBillInfo(FactoryBill bill, boolean confirmDuplicate) {

        // 检查是否存在相同的账单信息（按工厂 + 床号 + 计件单价，在配置的天数窗口内判重）
        if (!confirmDuplicate) {
            String duplicateDateTips = BillDuplicateCheckUtil.findDuplicateBillDates(
                    this,
                    bill.getFactoryId(),
                    bill.getNumber(),
                    bill.getPieceRateId(),
                    bill.getCreatedDate(),
                    null,
                    duplicateWindowDays
            );
            if (StrUtil.isNotBlank(duplicateDateTips)) {
                throw new BizException("保存失败：在所选日期（" + bill.getCreatedDate() + "）前后"
                        + duplicateWindowDays + "天内，已存在相同账单。重复日期：" + duplicateDateTips
                        + "。如果确实是返工/补做，请确认后强制保存。");
            }
        }

        // 计算账单价格（仅通过 pieceRateId + factoryId 关联计件单价）
        if (ObjectUtil.isNull(bill.getPieceRateId())) {
            throw new BizException("计件单价ID不能为空");
        }
        if (ObjectUtil.isNull(bill.getQuantity())) {
            throw new BizException("数量不能为空");
        }
        if (StrUtil.isBlank(bill.getNumber())) {
            throw new BizException("床号不能为空");
        }
        FactoryPieceRate matchedPrice = factoryPieceRateService.lambdaQuery()
                .eq(FactoryPieceRate::getId, bill.getPieceRateId())
                .eq(FactoryPieceRate::getFactoryId, bill.getFactoryId())
                .oneOpt()
                .orElseThrow(() -> new BizException("找不到对应的成衣厂计件单价信息，请检查计件单价ID与工厂ID是否正确"));
        if (matchedPrice.getPrice() == null) {
            throw new BizException("该计件单价没有填写单价，无法计算账单金额");
        }

        // 计算并保存账单价格：数量 × 单价，金额统一由 MoneyUtil 保留两位小数（四舍五入）。
        // 原来这一段包在 try/catch 里，catch 之后直接 return Result.fail——
        // 异常被吃掉，事务收不到异常不会回滚，日志里也没有堆栈。
        bill.setBill(MoneyUtil.scale(
                BigDecimal.valueOf(bill.getQuantity()).multiply(matchedPrice.getPrice())
        ));

        // 保存账单信息
        // 床 = (厂, 床号, 款式)，款式从计件单价上取；账单只挂 batchId
        Batch batch = batchService.findOrCreate(bill.getFactoryId(), bill.getNumber(), matchedPrice.getStyleNumber());
        bill.setBatchId(batch.getId());

        if (!save(bill)) {
            throw new BizException("保存账单失败，请重试");
        }

        return Result.ok();
    }

    /**
     * 修改账单信息
     *
     * @param bill 账单信息实体
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> updateFactoryBillInfo(FactoryBill bill, boolean confirmDuplicate) {

        // 判断传递过来的对象是否为空
        if (ObjectUtil.isEmpty(bill)) {
            throw new BizException("参数为空");
        }

        // 查询原账单，避免更新时未传 factoryId/quantity 导致计算失败
        FactoryBill originalBill = getById(bill.getId());
        if (ObjectUtil.isEmpty(originalBill)) {
            throw new BizException("账单不存在");
        }

        Integer factoryId = ObjectUtil.defaultIfNull(bill.getFactoryId(), originalBill.getFactoryId());
        Integer quantity = ObjectUtil.defaultIfNull(bill.getQuantity(), originalBill.getQuantity());
        String number = ObjectUtil.defaultIfNull(bill.getNumber(), originalBill.getNumber());
        Integer pieceRateId = ObjectUtil.defaultIfNull(bill.getPieceRateId(), originalBill.getPieceRateId());
        LocalDate selectedDate = ObjectUtil.defaultIfNull(bill.getCreatedDate(), originalBill.getCreatedDate());

        if (ObjectUtil.isNull(pieceRateId)) {
            throw new BizException("计件单价ID不能为空");
        }

        // 修改时同样做重复校验（排除当前账单自身），确认过则跳过
        if (!confirmDuplicate) {
            String duplicateDateTips = BillDuplicateCheckUtil.findDuplicateBillDates(
                    this,
                    factoryId,
                    number,
                    pieceRateId,
                    selectedDate,
                    originalBill.getId(),
                    duplicateWindowDays
            );
            if (StrUtil.isNotBlank(duplicateDateTips)) {
                throw new BizException("修改失败：在所选日期（" + selectedDate + "）前后"
                        + duplicateWindowDays + "天内，已存在相同账单。重复日期：" + duplicateDateTips
                        + "。如果确实是返工/补做，请确认后强制保存。");
            }
        }

        // 仅按 pieceRateId 重新计算，禁止按款式编号/工作类型回退匹配
        FactoryPieceRate matchedPrice = factoryPieceRateService.lambdaQuery()
                .eq(FactoryPieceRate::getId, pieceRateId)
                .eq(FactoryPieceRate::getFactoryId, factoryId)
                .oneOpt()
                .orElseThrow(() -> new BizException("找不到对应的成衣厂计件单价信息，请检查计件单价ID与工厂ID是否正确"));
        if (matchedPrice.getPrice() == null) {
            throw new BizException("该计件单价没有填写单价，无法计算账单金额");
        }
        if (quantity == null) {
            throw new BizException("数量不能为空");
        }

        // 回填 pieceRateId，保证账单与计件单价绑定一致
        bill.setPieceRateId(pieceRateId);

        // 数量乘以计件单价得出总账单，金额统一保留两位小数（四舍五入）
        bill.setBill(MoneyUtil.scale(BigDecimal.valueOf(quantity).multiply(matchedPrice.getPrice())));

        // 床号/款式/厂变了，先改床本身（它会同步工单和工作记录上的冗余列）；
        // 迁移前的老账单没有 batchId，这里补挂
        Integer batchId = originalBill.getBatchId();
        if (batchId == null) {
            batchId = batchService.findOrCreate(factoryId, number, matchedPrice.getStyleNumber()).getId();
        } else {
            batchService.renameBatch(batchId, factoryId, number, matchedPrice.getStyleNumber());
        }
        bill.setBatchId(batchId);

        // 修改
        if (!updateById(bill)) {
            throw new BizException("修改账单失败：该账单不存在，或已被其他人修改，请刷新后重试");
        }
        return Result.ok();
    }

    /**
     * 删除账单信息
     *
     * @param id 账单ID
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> deleteFactoryBillInfo(Integer id) {

        if (!removeById(id)) {
            throw new BizException("删除账单失败：该账单不存在或已被删除");
        }

        return Result.ok();
    }

    /**
     * 按条件查询要导出的成衣厂账单信息。
     * 日期排序按月份处理：未指定排序时默认按月份升序，选择创建日期时同样按月份排序。
     *
     * @param factoryId 成衣厂ID
     * @param startDate 起始日期
     * @param endDate   截止日期
     */
    @Override
    public List<FactoryBillDto> exportFactoryBillExcelByCondition(
            Integer factoryId,
            String startDate,
            String endDate,
            List<String> sortFields,
            List<String> sortOrders
    ) {
        startDate = normalizeDateParam(startDate);
        endDate = normalizeDateParam(endDate);

        FactoryBillExportSortUtil.ResolvedSort resolvedSort = FactoryBillExportSortUtil.resolve(sortFields, sortOrders);

        return factoryBillMapper.selectExportFactoryBillExcelByCondition(
                factoryId,
                startDate,
                endDate,
                resolvedSort.getColumns(),
                resolvedSort.getDirections()
        );
    }

    /**
     * 按成衣厂统计毛利（账单收入 - 工资支出）。
     *
     * <p>日期不传时按「本月 1 号到今天」，和其它报表的默认区间一致。</p>
     *
     * @param startDate 起始日期 yyyy-MM-dd，可为空
     * @param endDate   结束日期 yyyy-MM-dd，可为空
     * @return result
     */
    @Override
    public Result<Object> queryGrossProfit(String startDate, String endDate) {
        LocalDate today = LocalDate.now();
        LocalDate start = LocalDate.parse(Objects.requireNonNullElse(
                normalizeDateParam(startDate), today.withDayOfMonth(1).toString()));
        LocalDate end = LocalDate.parse(Objects.requireNonNullElse(
                normalizeDateParam(endDate), today.toString()));
        if (start.isAfter(end)) {
            throw new BizException("开始日期不能晚于结束日期");
        }
        return Result.ok(factoryBillMapper.selectGrossProfitByDate(start.toString(), end.toString()));
    }

    /**
     * 规整日期参数：
     * 1) 空值/空串/"0" 视为未传；
     * 2) 合法日期统一为 ISO yyyy-MM-dd；
     * 3) 非法日期视为未传，避免拼接到 SQL 中。
     */
    private String normalizeDateParam(String dateParam) {
        if (StrUtil.isBlank(dateParam)) {
            return null;
        }
        String value = dateParam.trim();
        if ("0".equals(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value).format(DateTimeFormatter.ISO_DATE);
        } catch (Exception exception) {
            return null;
        }
    }
}
