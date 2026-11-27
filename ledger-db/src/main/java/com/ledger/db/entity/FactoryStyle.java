package com.ledger.db.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ledger.common.entity.ParentEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>
 * 成衣厂款式表：一个成衣厂 + 一个款式编号一条，样板图片也存在这里
 * </p>
 *
 * @author ahui
 * @since 2026-11-27
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("factory_style")
public class FactoryStyle extends ParentEntity {

    @Serial
    private static final long serialVersionUID = 9021745308167482137L;

    /**
     * 成衣厂ID
     */
    private Integer factoryId;

    /**
     * 款式编号
     */
    private String styleNumber;

    /**
     * 样板图片访问路径
     */
    private String imagePath;
}
