package com.ledger.db.mapper;

import com.ledger.db.entity.dto.DashboardDailyDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 首页看板取数：只做按天聚合，合并与计算放在服务层。
 *
 * @author ahui
 */
@Mapper
public interface DashboardMapper {

    /**
     * 按天汇总账单件数与金额。
     *
     * @param startDate 起始日期 yyyy-MM-dd
     * @param endDate   结束日期 yyyy-MM-dd
     * @return 每天一行，按日期升序
     */
    @Select("select date_format(created_date, '%Y-%m-%d') as date, " +
            "coalesce(sum(quantity), 0) as billQty, " +
            "coalesce(sum(bill), 0) as billAmount " +
            "from factory_bill " +
            "where flag = 0 and created_date between #{startDate} and #{endDate} " +
            "group by created_date order by created_date")
    List<DashboardDailyDTO> selectDailyBill(@Param("startDate") String startDate,
                                            @Param("endDate") String endDate);

    /**
     * 按天汇总工资支出。
     *
     * @param startDate 起始日期 yyyy-MM-dd
     * @param endDate   结束日期 yyyy-MM-dd
     * @return 每天一行，按日期升序
     */
    @Select("select date_format(created_date, '%Y-%m-%d') as date, " +
            "coalesce(sum(salary), 0) as salary " +
            "from job " +
            "where flag = 0 and created_date between #{startDate} and #{endDate} " +
            "group by created_date order by created_date")
    List<DashboardDailyDTO> selectDailySalary(@Param("startDate") String startDate,
                                              @Param("endDate") String endDate);
}
