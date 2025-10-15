package com.ledger.db.entity.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author: ahui
 * @Description: TODO
 * @DateTime: 2025/10/11 - 10:23
 **/
@Data
public class JobCategoryDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -245121287721012980L;

    /**
     * ID
     */
    private Integer id;

    /**
     * 工作类型，例如 小花、大花、裤页
     */
    private String category;

    /**
     * 工作方式 ID，例如 1压花 2刮胶
     */
    private Integer modeId;

    /**
     * 工作方式，例如 压花、刮胶
     */
    private String mode;

}
