package com.ledger.db.service.factory;

import com.ledger.common.result.Result;
import com.ledger.db.entity.FactoryJobCategory;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 工作类型表 服务类
 * </p>
 *
 * @author ahui
 * @since 2026-01-23
 */
public interface IFactoryJobCategoryService extends IService<FactoryJobCategory> {

    Result<Object> queryFactoryJobCategoryList();

}
