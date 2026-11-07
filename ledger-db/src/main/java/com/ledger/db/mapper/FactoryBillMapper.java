package com.ledger.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.db.entity.FactoryBill;
import com.ledger.db.entity.dto.FactoryBillDto;
import com.ledger.db.entity.dto.FactoryGrossProfitDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 成衣厂账单表 Mapper 接口
 * </p>
 *
 * @author ahui
 * @since 2025-10-15
 */
@Mapper
public interface FactoryBillMapper extends BaseMapper<FactoryBill> {


    /**
     * 按条件查询成衣厂账单列表（款式来源于计件单价表）
     *
     * @param factoryId   工厂ID
     * @param number      床号
     * @param styleNumber 款式编号（计件单价表）
     * @param flag        删除状态 0否 1是
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @return page
     */
    Page<FactoryBillDto> selectFactoryBillList(
            Page<FactoryBillDto> page,
            @Param("factoryId") Integer factoryId, @Param("number") String number,
            @Param("styleNumber") String styleNumber,
            @Param("flag") Integer flag,
            @Param("startDate") String startDate, @Param("endDate") String endDate);


    /**
     * 获取指定工厂的账单可指定日期范围
     *
     * @param factoryId 工厂ID
     * @param startDate 起止日期
     * @param endDate   起止日期
     * @param flag      状态
     * @return factoryBillDto
     */
    FactoryBillDto calculateBillByFactoryIdAndDate(
            @Param("factoryId") Integer factoryId,
            @Param("startDate") String startDate, @Param("endDate") String endDate,
            @Param("flag") Integer flag
    );

    /**
     * 按条件获取要导出的成衣厂账单信息
     *
     * @param factoryId 工厂ID
     * @param startDate 起始日期
     * @param endDate   结束日期
     * @return list
     */
    List<FactoryBillDto> selectExportFactoryBillExcelByCondition(
            @Param("factoryId") Integer factoryId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("sortColumns") List<String> sortColumns,
            @Param("sortDirections") List<String> sortDirections
    );

    /**
     * 统计每家成衣厂在指定日期范围内的毛利（账单收入 - 工资支出）
     *
     * <p>收入来自 factory_bill.bill，工资来自 job.salary，按 factory_id 分别汇总后相减。
     * 只列还在用的成衣厂（factory.flag = 0），两边都没有数据的厂显示 0。</p>
     *
     * @param startDate 起始日期 yyyy-MM-dd
     * @param endDate   结束日期 yyyy-MM-dd
     * @return list
     */
    List<FactoryGrossProfitDTO> selectGrossProfitByDate(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

}
