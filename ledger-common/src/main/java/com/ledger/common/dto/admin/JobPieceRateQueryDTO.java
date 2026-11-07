package com.ledger.common.dto.admin;

import com.ledger.common.dto.PageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工计件单价查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JobPieceRateQueryDTO extends PageQueryDTO {

    

    /**
     * 工作方式ID。
     */
    private Integer modeId;

    /**
     * 工作类型ID。
     */
    private Integer categoryId;
}
