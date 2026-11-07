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
     * 员工默认的工作方式 1压花 2刮胶。
     *
     * <p>可以为空：一个人可能压花、刮胶都会做，档案上不再强制绑定唯一方式。
     * 认领工单时如果这里为空，就由前端选择本次按哪种方式计工资（WorkOrderClaimDTO.modeId）。</p>
     */
    private Integer modeId;
}
