package com.ledger.db.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.db.entity.FactoryQuotation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ledger.db.entity.dto.FactoryQuotationDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 成衣厂报价表 Mapper 接口
 * </p>
 *
 * @author ahui
 * @since 2025-10-16
 */
@Mapper
public interface FactoryQuotationMapper extends BaseMapper<FactoryQuotation> {

    /**
     * 按条件查询成衣厂报价表列表，默认查全部
     *
     * @param factoryId 成衣厂ID
     * @param categoryId 工作类型ID
     * @param flag 删除状态 0否 1是
     * @return page
     */
    Page<FactoryQuotationDto> selectFactoryQuotationListBtCondition(Page<FactoryQuotationDto> page, Integer factoryId, Integer categoryId, Integer flag);

}
