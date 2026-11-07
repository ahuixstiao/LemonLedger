package com.ledger.db.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ledger.common.entity.ParentEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * <p>
 * 成衣厂计件单价表
 * </p>
 *
 * @author ahui
 * @since 2025-10-16
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("factory_piece_rate")
public class FactoryPieceRate extends ParentEntity {

    @Serial
    private static final long serialVersionUID = 2749880619504267593L;

    /**
     * 成衣厂ID
     */
    private Integer factoryId;

    /**
     * 款式编号
     */
    private String styleNumber;

    /**
     * 工作类型 1小花、2大花、3裤页
     */
    private Integer categoryId;

    /**
     * 计件单价
     */
    private BigDecimal price;

    /**
     * 计件单价图片访问路径
     */
    private String imagePath;
}
