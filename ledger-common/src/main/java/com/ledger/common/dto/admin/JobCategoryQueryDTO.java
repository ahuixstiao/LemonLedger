package com.ledger.common.dto.admin;

import com.ledger.common.dto.PageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工作类型查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JobCategoryQueryDTO extends PageQueryDTO {

    /**
     * 工作类型名称。
     */
    private String category;
}
