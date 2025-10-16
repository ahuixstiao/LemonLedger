package com.ledger.db.impl.factory;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.common.result.Result;
import com.ledger.db.entity.Factory;
import com.ledger.db.mapper.FactoryMapper;
import com.ledger.db.service.factory.IFactoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FactoryServiceImpl extends ServiceImpl<FactoryMapper, Factory> implements IFactoryService {

    /**
     * 查询成衣厂列表
     *
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @return result
     */
    @Override
    public Result<Object> queryFactoryList(String factoryName, Integer currentPage, Integer pageSize, Integer flag) {

        Page<Factory> page = new Page<>(currentPage, pageSize);

        return Result.ok(
                lambdaQuery()
                        .eq(StrUtil.isNotBlank(factoryName), Factory::getFactoryName, factoryName)
                        .eq(Factory::getFlag, flag)
                        .page(page)
        );
    }

    /**
     * 保存成衣厂信息
     *
     * @param factory 成衣厂实体
     * @return result
     */
    @Override
    public Result<Object> saveFactoryInfo(Factory factory) {

        if (save(factory)) {
            return Result.ok();
        }

        return Result.fail();
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
