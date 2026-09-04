package com.ledger.db.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 成衣厂账单导出排序解析测试。
 */
class FactoryBillExportSortUtilTest {

    /**
     * 未指定排序字段时，应默认按账单月份升序。
     */
    @Test
    void resolveShouldDefaultToCreatedMonthAsc() {
        FactoryBillExportSortUtil.ResolvedSort resolvedSort = FactoryBillExportSortUtil.resolve(null, null);

        assertEquals(List.of(FactoryBillExportSortUtil.CREATED_MONTH_COLUMN), resolvedSort.getColumns());
        assertEquals(List.of("asc"), resolvedSort.getDirections());
    }

    /**
     * 选择创建日期时应映射为按月份排序，而不是按具体日期排序。
     */
    @Test
    void resolveShouldMapCreatedDateToMonthColumn() {
        FactoryBillExportSortUtil.ResolvedSort resolvedSort = FactoryBillExportSortUtil.resolve(
                List.of("createdDate"),
                List.of("desc")
        );

        assertEquals(List.of(FactoryBillExportSortUtil.CREATED_MONTH_COLUMN), resolvedSort.getColumns());
        assertEquals(List.of("desc"), resolvedSort.getDirections());
    }

    /**
     * 非法排序字段应被忽略，最终回退到按月份排序。
     */
    @Test
    void resolveShouldIgnoreUnknownFieldsAndFallbackToMonth() {
        FactoryBillExportSortUtil.ResolvedSort resolvedSort = FactoryBillExportSortUtil.resolve(
                List.of("unknownField"),
                List.of("desc")
        );

        assertEquals(List.of(FactoryBillExportSortUtil.CREATED_MONTH_COLUMN), resolvedSort.getColumns());
        assertEquals(List.of("asc"), resolvedSort.getDirections());
    }

    /**
     * 多字段排序时应保留白名单字段，并保持月份字段映射。
     */
    @Test
    void resolveShouldKeepWhitelistFieldsWithMonthMapping() {
        FactoryBillExportSortUtil.ResolvedSort resolvedSort = FactoryBillExportSortUtil.resolve(
                List.of("number", "createdDate", "bill"),
                List.of("desc", "asc", "DESC")
        );

        assertEquals(List.of("fb.number", FactoryBillExportSortUtil.CREATED_MONTH_COLUMN, "fb.bill"), resolvedSort.getColumns());
        assertEquals(List.of("desc", "asc", "desc"), resolvedSort.getDirections());
    }
}
