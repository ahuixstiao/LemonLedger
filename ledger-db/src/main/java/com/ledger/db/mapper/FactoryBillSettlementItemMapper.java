package com.ledger.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ledger.db.entity.FactoryBillSettlementItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 结算单明细 Mapper。
 *
 * @author ahui
 * @since 2026-11-22
 */
@Mapper
public interface FactoryBillSettlementItemMapper extends BaseMapper<FactoryBillSettlementItem> {
}
