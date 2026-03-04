package com.ledger.db.entity.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 工作报价列表展示DTO。
 */
@Data
public class JobQuotationDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -2457729323001478851L;

    private Integer id;

    /**
     * 工作方式ID
     */
    private Integer modeId;

    /**
     * 工作方式名称
     */
    private String mode;

    /**
     * 工作类型ID
     */
    private Integer categoryId;

    /**
     * 工作类型名称
     */
    private String category;

    /**
     * 报价
     */
    private BigDecimal quotation;

    /**
     * 创建日期
     */
    private String createdDate;

    /**
     * 数据状态 0正常 1删除
     */
    private Integer flag;
}
