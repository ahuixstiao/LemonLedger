package com.ledger.db.impl.factory;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ledger.common.result.Result;
import com.ledger.db.entity.FactoryJobCategory;
import com.ledger.db.mapper.FactoryJobCategoryMapper;
import com.ledger.db.service.factory.IFactoryJobCategoryService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 工作类型表 服务实现类
 * </p>
 *
 * @author ahui
 * @since 2026-01-23
 */
@Service
public class FactoryJobCategoryServiceImpl extends ServiceImpl<FactoryJobCategoryMapper, FactoryJobCategory> implements IFactoryJobCategoryService {

    /**
     * @return
     */
    @Override
    public Result<Object> queryFactoryJobCategoryList() {
        return Result.ok(lambdaQuery().list());
    }

    @Override
    public Result<Object> saveFactoryJobCategoryInfo(FactoryJobCategory factoryJobCategory) {
        return save(factoryJobCategory) ? Result.ok() : Result.fail();
    }

    @Override
    public Result<Object> updateFactoryJobCategoryInfo(FactoryJobCategory factoryJobCategory) {
        return updateById(factoryJobCategory) ? Result.ok() : Result.fail();
    }

    @Override
    public Result<Object> removeFactoryJobCategoryInfo(Integer categoryId) {
        return removeById(categoryId) ? Result.ok() : Result.fail();
    }
}
