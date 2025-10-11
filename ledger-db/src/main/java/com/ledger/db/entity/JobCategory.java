package com.ledger.db.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ledger.common.entity.ParentEntity;

import java.io.Serial;
import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 工作类型表
 * </p>
 *
 * @author ahui
 * @since 2025-10-02
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("job_category")
public class JobCategory extends ParentEntity {

    @Serial
    private static final long serialVersionUID = 3625128662045641565L;
    /**
     * 工作类型，例如 小花、大花、裤页
     */
    private String category;

    /**
     * 工作类型价格 根据工作方式来区分工作类型的工价
     */
    private BigDecimal price;

    /**
     * 工作方式 ID，例如 1压花 2刮胶
     */
    private Integer modeId;
}
