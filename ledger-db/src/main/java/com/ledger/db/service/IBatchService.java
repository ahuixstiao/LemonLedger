package com.ledger.db.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ledger.common.result.Result;
import com.ledger.db.entity.Batch;

/**
 * <p>
 * 床 服务类
 * </p>
 * <p>
 * 工单、账单、工作记录都挂 batch_id。录入时统一走 {@link #findOrCreate}：
 * 同样的 (厂, 床号, 款式) 只留一条床记录，不会因为同一天开两次单就分裂成两张床。
 * </p>
 *
 * @author ahui
 * @since 2026-11-06
 */
public interface IBatchService extends IService<Batch> {

    /**
     * 按 (厂, 床号, 款式) 找床，没有就建一张。
     *
     * @param factoryId   成衣厂ID
     * @param number      床号
     * @param styleNumber 款式编号
     * @return 已存在的或新建的床
     */
    Batch findOrCreate(Integer factoryId, String number, String styleNumber);

    /**
     * 改床号/款式，并把工单、账单、工作记录上的冗余列一起刷新。
     *
     * <p>如果目标 (厂, 床号, 款式) 已经有另一张床（比如同一床号下有过两个款式），
     * 不再报“已经有一张床了”，而是直接把那张床返回，调用方把单据挂过去即可。</p>
     *
     * @param batchId     床ID
     * @param factoryId   成衣厂ID
     * @param number      新床号
     * @param styleNumber 新款号
     * @return 这条单据最终应该挂的床
     */
    Batch renameBatch(Integer batchId, Integer factoryId, String number, String styleNumber);

    /**
     * 分页查询床列表
     *
     * @param factoryId   成衣厂ID
     * @param number      床号（模糊）
     * @param styleNumber 款式编号（模糊）
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @return result
     */
    Result<Object> queryBatchList(Integer factoryId, String number, String styleNumber,
                                  Integer currentPage, Integer pageSize);

    /**
     * 床上一张单据（账单/工单/工作记录）都不剩时，把它一起逻辑删除，避免床列表里堆积空床。
     *
     * @param batchId 床ID
     */
    void deleteIfEmpty(Integer batchId);

    /**
     * 删除床（逻辑删除），被单据引用时拒绝
     *
     * @param batchId 床ID
     * @return result
     */
    Result<Object> deleteBatch(Integer batchId);
}
