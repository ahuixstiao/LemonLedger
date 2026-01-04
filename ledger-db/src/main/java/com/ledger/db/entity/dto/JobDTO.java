package com.ledger.db.entity.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author: ahui
 * @Description: TODO 工作信息聚合实体 DTO一般用于表现层与服务层之间的数据传输
 * @DateTime: 2025/10/3 - 22:29
 **/
@Data
public class JobDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 7293007662217256361L;

    // job实体属性
    private Integer id;
    /**
     * 员工ID
     */
    private Integer employeeId;
    /**
     * 员工姓名
     */
    private String name;
    /**
     * 成衣厂ID
     */
    private Integer factoryId;
    /**
     * 成衣厂名称
     */
    private String factoryName;

    /**
     * 床号
     */
    private String number;

    /**
     * 款式编号 比如3101
     */
    private String styleNumber;

    /**
     * 工作方式ID
     */
    private Integer modeId;
    /**
     * 工作方式 例如 1压花、2刮胶
     */
    private String mode;
    /**
     * 工作方式 工价
     */
    private BigDecimal modePrice;


    /**
     * 工作类型ID
     */
    private Integer categoryId;
    /**
     * 工作类型 例如 1小花、2大花、3裤页
     */
    private String category;
    /**
     * 工作类型 工价
     */
    private BigDecimal categoryPrice;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 工作薪酬
     */
    private BigDecimal salary;

    /**
     * 创建时间
     */
    private String createdDate;

}
