package com.ledger.db.entity.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author: ahui
 * @Description: TODO 工厂账单DTO
 * @DateTime: 2025/10/15 - 23:04
 **/
@Data
public class FactoryBillDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -8681847460603512795L;

    /**
     * 成衣厂ID
     */
    private Integer factoryId;
    /**
     * 成衣厂名称
     */
    private String factoryName;

    /**
     * 床号
     */
    private Integer number;

    /**
     * 款式编号
     */
    private Integer styleNumber;

    /**
     * 类型 小花、大花、裤页
     */
    private Integer categoryId;
    /**
     * 工作类型名称
     */
    private String category;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 本床账单
     */
    private BigDecimal bill;



}
