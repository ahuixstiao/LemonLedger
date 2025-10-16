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
 * 成衣厂报价表
 * </p>
 *
 * @author ahui
 * @since 2025-10-16
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("factory_quotation")
public class FactoryQuotation extends ParentEntity {

    @Serial
    private static final long serialVersionUID = 2749880619504267593L;

    /**
     * 成衣厂ID
     */
    private Integer factoryId;

    /**
     * 工作类型 1小花、2大花、3裤页
     */
    private Integer categoryId;

    /**
     * 报价
     */
    private BigDecimal quotation;
}
