package com.ledger.db.entity.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 工作工单聚合 DTO（含厂名、工作类型名称）
 */
@Data
public class JobWorkOrderDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -2168581292106451656L;

    private Integer id;
    private Integer factoryId;
    private String factoryName;
    private String number;
    private String styleNumber;
    private Integer categoryId;
    private String category;
    private Integer quantity;
    private String createdDate;
    private Integer flag;
}
