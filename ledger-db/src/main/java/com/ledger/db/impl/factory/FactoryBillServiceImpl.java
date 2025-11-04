package com.ledger.db.impl.factory;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ledger.common.result.Result;
import com.ledger.db.entity.FactoryBill;
import com.ledger.db.entity.FactoryQuotation;
import com.ledger.db.entity.dto.FactoryBillDto;
import com.ledger.db.mapper.FactoryBillMapper;
import com.ledger.db.service.factory.IFactoryBillService;
import com.ledger.db.service.factory.IFactoryQuotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * <p>
 * 成衣厂账单表 服务实现类
 * </p>
 *
 * @author ahui
 * @since 2025-10-15
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FactoryBillServiceImpl extends ServiceImpl<FactoryBillMapper, FactoryBill> implements IFactoryBillService {

    private final FactoryBillMapper factoryBillMapper;

    private final IFactoryQuotationService factoryQuotationService;

    /**
     * 按条件查询成衣厂账单列表  如果不传递参数则默认查询全部成衣厂账单列表
     *
     * @param factoryId   工厂ID
     * @param number      床号
     * @param styleNumber 款式编号
     * @param categoryId  工作类型
     * @param flag        删除状态 0否 1是
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @param currentPage 当前页
     * @param pageSize    页面条数
     * @return result
     */
    @Override
    @Transactional(readOnly = true)
    public Result<Object> queryFactoryBillListByCondition(Integer factoryId, String number, String styleNumber, Integer categoryId, Integer flag, String startDate, String endDate, Integer currentPage, Integer pageSize) {

        // 如果指定时间范围则按范围查询
        if (StrUtil.isNotBlank(startDate) & StrUtil.isNotBlank(endDate)) {
            // 格式化前端传入的时间 YYYY-MM-DD
            startDate = LocalDate.parse(startDate).format(DateTimeFormatter.ISO_DATE);
            endDate = LocalDate.parse(endDate).format(DateTimeFormatter.ISO_DATE);
        }

        Page<FactoryBillDto> page = new Page<>(currentPage, pageSize);

        page = factoryBillMapper.selectFactoryBillList(
                page,
                factoryId, number, styleNumber, categoryId, flag,
                startDate, endDate);

        if (page.getRecords().isEmpty()) {
            return Result.fail("暂无数据");
        }

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
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Object> saveFactoryBillInfo(FactoryBill bill) {

        // 检查是否存在相同的账单信息
        boolean billExists = lambdaQuery()
                .eq(FactoryBill::getFactoryId, bill.getFactoryId())
                .eq(FactoryBill::getNumber, bill.getNumber())
                .eq(FactoryBill::getStyleNumber, bill.getStyleNumber())
                .eq(FactoryBill::getCategoryId, bill.getCategoryId())
                .eq(FactoryBill::getCreatedTime, bill.getCreatedTime())
                .eq(FactoryBill::getFlag, 0)
                .exists();

        if (billExists) {
            return Result.fail("账单已存在");
        }

        // 计算账单价格
        // 计算方式 1、先获取成衣厂报价表对应成衣厂的款式编号和工作类型的报价再乘以数量
        BigDecimal factoryQuotation = factoryQuotationService.lambdaQuery()
                .eq(FactoryQuotation::getFactoryId, bill.getFactoryId())
                .eq(FactoryQuotation::getStyleNumber, bill.getStyleNumber())
                .eq(FactoryQuotation::getCategoryId, bill.getCategoryId())
                .oneOpt()
                .orElseThrow(() -> new RuntimeException("找不到对应的成衣厂报价信息"))
                .getQuotation();

        // 计算并保存账单价格
        bill.setBill(
                BigDecimal
                        .valueOf(bill.getQuantity())
                        .multiply(factoryQuotation)
                        .setScale(2, RoundingMode.HALF_UP)
        );

        // 保存账单信息
        if (save(bill)) {
            return Result.ok();
        }

        return Result.fail("保存失败");
    }

    /**
     * 修改账单信息
     *
     * @param bill 账单信息实体
     * @return result
     */
    @Override
    public Result<Object> updateFactoryBillInfo(FactoryBill bill) {

        // 判断传递过来的对象是否为空
        if (ObjectUtil.isEmpty(bill)) {
            return Result.fail("参数为空");
        }

        if (StrUtil.isNotBlank(bill.getStyleNumber()) | ObjectUtil.isNotNull(bill.getCategoryId())){
            // 获取成衣厂报价
            BigDecimal quotation = factoryQuotationService.lambdaQuery()
                    .eq(FactoryQuotation::getFactoryId, bill.getFactoryId())
                    // 判断是否传递了款式编号或工作类型其中一个否则条件构成失败
                    .eq(StrUtil.isNotBlank(bill.getStyleNumber()), FactoryQuotation::getStyleNumber, bill.getStyleNumber())
                    .eq(ObjectUtil.isNotEmpty(bill.getCategoryId()), FactoryQuotation::getCategoryId, bill.getCategoryId())
                    .oneOpt().orElseThrow(() -> new RuntimeException("暂无报价信息")).getQuotation();

            //数量乘以报价得出总账单
            bill.setBill(
                    BigDecimal.valueOf(bill.getQuantity()).multiply(quotation).setScale(2, RoundingMode.HALF_UP)
            );
        }
        // 修改
        if (updateById(bill)) {
            return Result.ok();
        }

        return Result.fail();
    }

    /**
     * 删除账单信息
     *
     * @param factoryBillId 账单ID
     * @return result
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Object> deleteFactoryBillInfo(Integer factoryBillId) {

        if (removeById(factoryBillId)) {
            return Result.ok();
        }

        return Result.fail("删除失败");
    }
}
