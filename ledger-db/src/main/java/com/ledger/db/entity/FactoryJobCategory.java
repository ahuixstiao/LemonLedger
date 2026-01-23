package com.ledger.db.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ledger.common.entity.ParentEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>
 * 工作类型表
 * </p>
 *
 * @author ahui
 * @since 2026-01-23
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("factory_job_category")
public class FactoryJobCategory extends ParentEntity {

    @Serial
    private static final long serialVersionUID = -9203043351809741983L;
    /**
     * 工作类型，例如 小花、大花、裤页
     */
    private String category;
}
