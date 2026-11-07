package com.ledger.db.service.factory;

import com.ledger.common.result.Result;
import com.ledger.db.entity.FactoryBill;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ledger.db.entity.dto.FactoryBillDto;

import java.util.List;

/**
 * <p>
 * 成衣厂账单表 服务类
 * </p>
 *
 * @author ahui
 * @since 2025-10-15
 */
public interface IFactoryBillService extends IService<FactoryBill> {

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
    Result<Object> queryFactoryBillListByCondition(
            Integer factoryId, String number,
            String styleNumber, Integer flag,
            String startDate, String endDate,
            Integer currentPage, Integer pageSize);

    /**
     * 统计成衣厂账单
     *
     * @param factoryId 成衣厂ID
     * @param startDate 起止日期
     * @param endDate   起止日期
     * @param flag      状态
     * @return result
     */
    Result<Object> statisticalBill(Integer factoryId, String startDate, String endDate, Integer flag);

    /**
     * 保存账单信息
     *
     * @param bill             账单实体
     * @param confirmDuplicate 已确认"确实要重复记账"（返工、补做），跳过前后窗口内的重复校验
     * @return result
     */
    Result<Object> saveFactoryBillInfo(FactoryBill bill, boolean confirmDuplicate);

    /**
     * 修改账单信息
     *
     * @param bill             账单信息实体
     * @param confirmDuplicate 已确认"确实要重复记账"，跳过重复校验
     * @return result
     */
    Result<Object> updateFactoryBillInfo(FactoryBill bill, boolean confirmDuplicate);

    /**
     * 删除账单信息
     *
     * @param id 账单ID
     * @return result
     */
    Result<Object> deleteFactoryBillInfo(Integer id);

    /**
     * 按条件导出成衣厂账单 Excel
     *
     * @param factoryId 成衣厂ID
     * @param startDate 起始日期
     * @param endDate   截止日期
     */
    List<FactoryBillDto> exportFactoryBillExcelByCondition(
            Integer factoryId,
            String startDate,
            String endDate,
            java.util.List<String> sortFields,
            java.util.List<String> sortOrders
    );

    /**
     * 按成衣厂统计毛利（账单收入 - 工资支出）
     *
     * <p>日期都不传时按「本月 1 号到今天」，和报表页其它统计保持一致。</p>
     *
     * @param startDate 起始日期 yyyy-MM-dd，可为空
     * @param endDate   结束日期 yyyy-MM-dd，可为空
     * @return result，data 里是 {@code List<FactoryGrossProfitDTO>}
     */
    Result<Object> queryGrossProfit(String startDate, String endDate);
}
