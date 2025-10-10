package com.ledger.db.service.impl;

import com.ledger.common.result.Result;
import com.ledger.db.entity.Factory;
import com.ledger.db.mapper.FactoryMapper;
import com.ledger.db.service.IFactoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 成衣厂表 服务实现类
 * </p>
 *
 * @author ahui
 * @since 2025-10-03
 */
@Service
public class FactoryServiceImpl extends ServiceImpl<FactoryMapper, Factory> implements IFactoryService {

    /**
     * 查询成衣厂列表
     *
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @return result
     */
    @Override
    public Result<Object> queryFactoryList(Integer currentPage, Integer pageSize) {
        return null;
    }

    /**
     * 保存成衣厂信息
     *
     * @param factory 成衣厂实体
     * @return result
     */
    @Override
    public Result<Object> saveFactory(Factory factory) {
        return null;
    }

    /**
     * 更新成衣厂信息
     *
     * @param factory 成衣厂实体
     * @return result
     */
    @Override
    public Result<Object> updateFactory(Factory factory) {
        return null;
    }
}
