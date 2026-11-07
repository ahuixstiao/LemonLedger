package com.ledger.db.entity.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 工单认领进度：某个工单在某种工作方式下，已经认领了多少、还剩多少。
 *
 * <p>认领是按「工作方式」分别计数的（压花和刮胶各有各的数量上限），
 * 所以这里必须带上 modeId，不能只给一个总剩余。</p>
 */
@Data
public class JobWorkOrderProgressDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 4102881926531435561L;

    /** 工单ID */
    private Integer workOrderId;

    /** 工单预设数量 */
    private Integer quantity;

    /** 本次统计使用的工作方式ID */
    private Integer modeId;

    /** 该工作方式下已认领数量 */
    private Integer claimedQuantity;

    /** 该工作方式下剩余可认领数量（不足 0 时按 0 返回） */
    private Integer remainQuantity;
}
