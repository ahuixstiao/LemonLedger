package com.ledger.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.db.entity.FactoryBill;
import com.ledger.db.entity.dto.FactoryBillDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
     * 按条件查询成衣厂账单列表  如果不传递参数则默认查询全部成衣厂账单列表
     *
     * @param factoryId   工厂ID
     * @param number      床号
     * @param styleNumber 款式编号
     * @param categoryId  工作类型
     * @param flag        删除状态 0否 1是
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @return page
     */
    Page<FactoryBillDto> selectFactoryBillList(
            Page<FactoryBillDto> page,
            @Param("factoryId") Integer factoryId, @Param("number") String number,
            @Param("styleNumber") String styleNumber, @Param("categoryId") Integer categoryId,
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
            @Param("flag") Integer flag);

}
