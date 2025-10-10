package com.ledger.db.entity;

import com.ledger.common.entity.ParentEntity;

import java.io.Serial;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 成衣厂表
 * </p>
 *
 * @author ahui
 * @since 2025-10-03
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Factory extends ParentEntity {

    @Serial
    private static final long serialVersionUID = -5150006857926358331L;
    /**
     * 成衣厂名称
     */
    private String factoryName;
}
