package com.ledger.common.dto.admin;

import com.ledger.common.dto.PageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeeQueryDTO extends PageQueryDTO {

    /**
     * 员工姓名。
     */
    private String name;
}
