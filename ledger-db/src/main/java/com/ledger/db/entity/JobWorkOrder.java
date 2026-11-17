package com.ledger.db.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ledger.common.entity.ParentEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>
 * 工作工单表
 * </p>
 *
 * @author ahui
 * @since 2026-02-26
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("job_work_order")
public class JobWorkOrder extends ParentEntity {

    @Serial
    private static final long serialVersionUID = -8762816869091164595L;

    /**
     * 成衣厂ID
     */
    private Integer factoryId;

    /**
     * 床号
     */
    private String number;

    /**
     * 款式编号
     */
    private String styleNumber;

    /**
     * 工作类型，例如 小花、大花、裤页
     */
    private Integer categoryId;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 已认领数量（压花，mode_id=1）。
     * 与 job 明细在同一个事务里增减，认领时用「剩余够不够」作为 UPDATE 条件，
     * 让判断和扣减在同一条 SQL 里完成，避免并发双领。
     */
    private Integer claimedEmboss;

    /**
     * 已认领数量（刮胶，mode_id=2）
     */
    private Integer claimedGlue;

    /**
     * 床ID，关联 batch.id（床号 / 成衣厂 / 款式的唯一事实来源）
     */
    private Integer batchId;
}
