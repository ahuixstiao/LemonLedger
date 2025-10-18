package com.ledger.db.entity;

import com.ledger.common.entity.ParentEntity;

import java.io.Serial;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 员工表
 * </p>
 *
 * @author ahui
 * @since 2025-10-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Employee extends ParentEntity {

    @Serial
    private static final long serialVersionUID = -3798320382232790041L;
    /**
     * 员工姓名，用于查询工作内容
     */
    private String name;

    /**
     * 员工的工作方式 1压花 2刮胶
     */
    private Integer modeId;
}
