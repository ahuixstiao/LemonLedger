package com.ledger.common.dto.admin;

import com.ledger.common.dto.PageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 成衣厂查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FactoryQueryDTO extends PageQueryDTO {

    /**
     * 工厂名称。
     */
    private String factoryName;
}
