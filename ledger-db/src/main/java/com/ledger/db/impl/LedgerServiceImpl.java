package com.ledger.db.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ledger.common.entity.ParentEntity;

/**
 * 带乐观锁兜底的 Service 基类。
 *
 * <p>背景：实体上有 {@code @Version}，MyBatis-Plus 只会在
 * {@code version != null} 时拼 {@code where id = ? and version = ?}。
 * 而前端的所有编辑弹窗都不回传 version，于是这个乐观锁一直是摆设：
 * 两个人（或两个页签）同时改同一行，后提交的会把先提交的覆盖掉，谁也不报错。</p>
 *
 * <p>这里的做法：更新时如果调用方没带 version，就先用主键读一次当前 version 再更新。
 * 这是「读后 CAS」——真正并发时，B 读到的还是旧 version，
 * B 的 update 会因为 {@code version = ?} 对不上而影响 0 行，
 * 前端会收到「数据已被修改，请刷新后重试」，而不是悄悄覆盖。</p>
 *
 * <p>注意：这里补的是「最后一道保险」，正常请求仍应让前端把 version 传回来，
 * 这样连读一次都能省掉。</p>
 *
 * @param <M> Mapper 类型
 * @param <T> 实体类型
 */
public abstract class LedgerServiceImpl<M extends BaseMapper<T>, T extends ParentEntity>
        extends ServiceImpl<M, T> {

    /**
     * 更新单条记录；调用方未传 version 时自动补上数据库中的当前版本。
     *
     * @param entity 待更新实体
     * @return 是否更新成功（false 表示记录不存在或被并发改过）
     */
    @Override
    public boolean updateById(T entity) {
        if (entity != null && entity.getId() != null && entity.getVersion() == null) {
            T current = getById(entity.getId());
            if (current != null && current.getVersion() != null) {
                entity.setVersion(current.getVersion());
            }
        }
        return super.updateById(entity);
    }
}
