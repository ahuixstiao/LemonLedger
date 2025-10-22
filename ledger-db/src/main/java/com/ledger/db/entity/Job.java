package com.ledger.db.entity;

import com.ledger.common.entity.ParentEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * <p>
 * 工作内容表
 * </p>
 *
 * @author ahui
 * @since 2025-10-02
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Job extends ParentEntity {

    @Serial
    private static final long serialVersionUID = -7070688770607819133L;
    /**
     * 员工ID
     */
    private Integer employeeId;

    /**
    * 成衣厂ID
    */
    private Integer factoryId;

    /**
     * 床号
     */
    private String number;

    /**
     * 款式编号 比如3101
     */
    private Integer styleNumber;

    /**
     * 工作方式，例如 压花、刮胶
     */
    private Integer modeId;

    /**
     * 工作类型，例如 小花、大花、裤页
     */
    private Integer categoryId;

    /**
     * 工作数量
     */
    private Integer quantity;

    /**
     * 本床薪资
     */
    private BigDecimal salary;
}
