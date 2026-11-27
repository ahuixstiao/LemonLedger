package com.ledger.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.db.entity.FactoryStyle;
import com.ledger.db.entity.dto.FactoryPieceRateDto;
import com.ledger.db.entity.dto.FactoryStyleDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 成衣厂款式表 Mapper 接口
 * </p>
 *
 * @author ahui
 * @since 2026-11-27
 */
@Mapper
public interface FactoryStyleMapper extends BaseMapper<FactoryStyle> {

    /**
     * 按条件分页查询成衣厂款式
     *
     * @param page        分页对象
     * @param factoryId   成衣厂ID
     * @param styleNumber 款式编号（模糊匹配）
     * @param flag          删除状态 0否 1是
     * @param onlyWithImage 是否只查有样板图片的款式
     * @return page
     */
    Page<FactoryStyleDto> selectFactoryStyleListByCondition(
            Page<FactoryStyleDto> page,
            @Param("factoryId") Integer factoryId,
            @Param("styleNumber") String styleNumber,
            @Param("flag") Integer flag,
            @Param("onlyWithImage") Boolean onlyWithImage);

    /**
     * 按成衣厂查款式下拉列表
     *
     * @param factoryId 成衣厂ID
     * @param flag      删除状态 0否 1是
     * @return list
     */
    List<FactoryStyleDto> selectStyleListByFactoryId(
            @Param("factoryId") Integer factoryId,
            @Param("flag") Integer flag);

    /**
     * 员工端按款式编号模糊查询样板图片
     *
     * @param styleNumber 款式编号（可为空）
     * @param flag        删除状态 0否 1是
     * @return list
     */
    List<FactoryPieceRateDto> selectSampleListByStyleNumber(
            @Param("styleNumber") String styleNumber,
            @Param("flag") Integer flag);
}
