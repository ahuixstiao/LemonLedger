package com.ledger.db.mapper;

import com.ledger.db.entity.FactoryBill;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ledger.db.entity.dto.FactoryBillDto;
import org.apache.ibatis.annotations.Mapper;

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


    List<FactoryBillDto> queryFactoryBillList(Integer flag);

}
