package com.ledger.db.service;

import com.ledger.common.result.Result;
import com.ledger.db.entity.Factory;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 成衣厂表 服务类
 * </p>
 *
 * @author ahui
 * @since 2025-10-03
 */
public interface IFactoryService extends IService<Factory> {


    /**
     * 查询成衣厂列表
     * @param factoryName 成衣厂名称
     * @param flag        删除状态 0正常 1删除
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @return result
     */
    Result<Object> queryFactoryList(String factoryName, Integer currentPage, Integer pageSize, Integer flag);

    /**
     * 保存成衣厂信息
     *
     * @param factory 成衣厂实体
     * @return result
     */
    Result<Object> saveFactoryInfo(Factory factory);

    /**
     * 更新成衣厂信息
     *
     * @param factory 成衣厂实体
     * @return result
     */
    Result<Object> updateFactory(Factory factory);


}
