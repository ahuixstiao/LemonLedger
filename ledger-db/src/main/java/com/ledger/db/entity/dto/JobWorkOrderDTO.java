package com.ledger.db.entity.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 工作工单聚合 DTO（含厂名、工作类型名称）
 */
@Data
public class JobWorkOrderDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -2168581292106451656L;

    private Integer id;
    private Integer factoryId;
    private String factoryName;
    private String number;
    private String styleNumber;
    private Integer categoryId;
    private String category;
    private Integer quantity;
    private String createdDate;
    private Integer flag;

    /**
     * 已完成数量：取「压花/刮胶」两个工种里做得少的那个。
     *
     * <p>一床布压花、刮胶两道工序都要各做一遍，一件只有两道都领过才算做完；
     * 两个工种直接相加会得到 2 倍数量，剩余就成了负数，所以这里取较小值。</p>
     */
    private Integer claimedQuantity;

    /** 压花（工作方式 1）已认领数量 */
    private Integer claimedEmboss;

    /** 刮胶（工作方式 2）已认领数量 */
    private Integer claimedGlue;

    /** 剩余数量 = 预设数量 - 已完成数量（两道工序都做完才算完成），最小为 0 */
    private Integer remainQuantity;
}
