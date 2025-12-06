package com.ledger.db.entity.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author: ahui
 * @Description: TODO
 * @DateTime: 2025/10/16 - 14:55
 **/
@Data
public class FactoryQuotationDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -6940791438457492925L;

    private Integer id;

    /**
     * 成衣厂ID
     */
    private Integer factoryId;

    /**
     * 成衣厂名称
     */
    private String factoryName;

    /**
     * 款式编号
     */
    private String styleNumber;

    /**
     * 工作类型 1小花、2大花、3裤页
     */
    private Integer categoryId;

    /**
     * 工作类型名称
     */
    private String category;

    /**
     * 报价
     */
    private BigDecimal quotation;

}
