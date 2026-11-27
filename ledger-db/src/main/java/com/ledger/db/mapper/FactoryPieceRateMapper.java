package com.ledger.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.db.entity.FactoryPieceRate;
import com.ledger.db.entity.dto.FactoryPieceRateDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 成衣厂计件单价表 Mapper 接口
 * </p>
 *
 * @author ahui
 * @since 2025-10-16
 */
@Mapper
public interface FactoryPieceRateMapper extends BaseMapper<FactoryPieceRate> {

    /**
     * 按条件查询成衣厂计件单价表列表，默认查全部
     *
     * @param factoryId   成衣厂ID
     * @param styleNumber 款式编号（经 factory_style 关联匹配）
     * @param categoryId  工作类型ID
     * @param flag        删除状态 0否 1是
     * @return page
     */
    Page<FactoryPieceRateDto> selectFactoryPieceRateListByCondition(
            Page<FactoryPieceRateDto> page,
            @Param("factoryId") Integer factoryId,
            @Param("styleNumber")String styleNumber,
            @Param("categoryId") Integer categoryId,
            @Param("flag") Integer flag);

    /**
     * 账单用：某成衣厂的计件单价下拉项
     *
     * @param factoryId 成衣厂ID
     * @param flag      删除状态 0否 1是
     * @return list
     */
    List<FactoryPieceRateDto> selectPieceRateOptionsByFactoryId(
            @Param("factoryId") Integer factoryId,
            @Param("flag") Integer flag);

}
