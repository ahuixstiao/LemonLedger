package com.ledger.common.dto.admin;

import com.ledger.common.dto.PageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工作方式查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JobModeQueryDTO extends PageQueryDTO {

    /**
     * 工作方式名称。
     */
    private String mode;
}
