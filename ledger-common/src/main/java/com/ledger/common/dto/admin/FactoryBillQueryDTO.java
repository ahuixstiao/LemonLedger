package com.ledger.common.dto.admin;

import com.ledger.common.dto.PageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 成衣厂账单查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FactoryBillQueryDTO extends PageQueryDTO {

    /**
     * 成衣厂ID。
     */
    private Integer factoryId;

    /**
     * 床号。
     */
    private String number;

    /**
     * 款式编号。
     */
    private String styleNumber;

    /**
     * 工作类型ID。
     */
    private Integer categoryId;

    /**
     * 起始日期。
     */
    private String startDate;

    /**
     * 结束日期。
     */
    private String endDate;
}
