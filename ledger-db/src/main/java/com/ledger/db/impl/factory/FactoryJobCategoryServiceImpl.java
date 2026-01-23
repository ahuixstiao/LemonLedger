package com.ledger.db.impl.factory;

import com.ledger.common.result.Result;
import com.ledger.db.entity.FactoryJobCategory;
import com.ledger.db.mapper.FactoryJobCategoryMapper;
import com.ledger.db.service.factory.IFactoryJobCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
}
