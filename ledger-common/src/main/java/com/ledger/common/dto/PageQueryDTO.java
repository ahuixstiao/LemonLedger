package com.ledger.common.dto;

import lombok.Data;

/**
 * 通用分页查询参数。
 */
@Data
public class PageQueryDTO {

    /**
     * 当前页。
     */
    private Integer currentPage = 1;

    /**
     * 每页条数。
     */
    private Integer pageSize = 10;

    /**
     * 创建日期 格式：yyyy-MM-dd
     */
    private String createdDate;

    /**
     * 逻辑删除标识，0-正常，1-删除。
     */
    private Integer flag = 0;
}