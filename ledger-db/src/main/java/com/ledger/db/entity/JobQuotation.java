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
 * 工作报价表
 * </p>
 *
 * @author ahui
 * @since 2025-10-16
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("job_quotation")
public class JobQuotation extends ParentEntity {

    @Serial
    private static final long serialVersionUID = 2060458083488099655L;

    /**
     * 工作方式
     */
    private Integer modeId;

    /**
     * 工作类型 1小花、2大花、3裤页
     */
    private Integer categoryId;

    /**
     * 报价
     */
    private BigDecimal quotation;
}
