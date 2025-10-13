package com.ledger.db.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ledger.common.entity.ParentEntity;

import java.io.Serial;
import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 工作方式表
 * </p>
 *
 * @author ahui
 * @since 2025-10-02
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("job_mode")
public class JobMode extends ParentEntity {

    @Serial
    private static final long serialVersionUID = 2111013045403443199L;
    /**
     * 工作方式，例如 压花、刮胶
     */
    private String mode;

    /**
     * 工作方式 工价
     */
    private BigDecimal modePrice;
}
