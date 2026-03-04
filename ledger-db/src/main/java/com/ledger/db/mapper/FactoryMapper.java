package com.ledger.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.db.entity.Factory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 成衣厂表 Mapper 接口
 * </p>
 *
 * @author ahui
 * @since 2025-10-03
 */
@Mapper
public interface FactoryMapper extends BaseMapper<Factory> {

    /**
     * 按条件分页查询成衣厂列表（自定义 SQL，支持查询逻辑删除数据）。
     *
     * @param page        分页参数
     * @param factoryName 工厂名称
     * @param flag        删除状态 0正常 1删除
     * @return 分页结果
     */
    Page<Factory> queryFactoryList(Page<Factory> page,
                                   @Param("factoryName") String factoryName,
                                   @Param("flag") Integer flag);
}
