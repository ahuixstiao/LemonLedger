package com.ledger.db.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ledger.common.entity.ParentEntity;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 成衣厂账单表
 * </p>
 *
 * @author ahui
 * @since 2025-10-15
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("factory_bill")
public class FactoryBill extends ParentEntity {

    @Serial
    private static final long serialVersionUID = 3106510113334294936L;

    /**
     * 成衣厂ID
     */
    private Integer factoryId;

    /**
     * 床号
     */
    private Integer number;

    /**
     * 款式编号
     */
    private Integer styleNumber;

    /**
     * 工作类型 1、小花、2、大花、3、裤页
     */
    private Integer categoryId;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 本床账单
     */
    private BigDecimal bill;
}
