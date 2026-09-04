package com.ledger.db.util;

import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 成衣厂账单导出排序解析工具。
 * 日期相关排序统一按月份处理，避免按具体日期打散同一月份的账单。
 */
public final class FactoryBillExportSortUtil {

    /**
     * 按月份排序的安全列标识，Mapper 会将其映射为 DATE_FORMAT(fb.created_date, '%Y-%m')。
     */
    public static final String CREATED_MONTH_COLUMN = "fb.created_month";

    private static final Map<String, String> ALLOWED_SORT_COLUMNS = Map.of(
            "factoryName", "factory.factory_name",
            "number", "fb.number",
            "styleNumber", "fq.style_number",
            "category", "fjc.category",
            "quantity", "fb.quantity",
            "quotation", "fq.quotation",
            "bill", "fb.bill",
            "createdDate", CREATED_MONTH_COLUMN
    );

    private FactoryBillExportSortUtil() {
    }

    /**
     * 将前端传入的排序字段解析为白名单列名与方向。
     * 未指定有效排序字段时，默认按账单月份升序。
     *
     * @param sortFields  前端排序字段
     * @param sortOrders  与字段一一对应的排序方向
     * @return 解析后的安全排序配置
     */
    public static ResolvedSort resolve(List<String> sortFields, List<String> sortOrders) {
        List<String> safeSortColumns = new ArrayList<>();
        List<String> safeSortDirections = new ArrayList<>();

        if (sortFields != null && !sortFields.isEmpty()) {
            for (int i = 0; i < sortFields.size(); i++) {
                String field = sortFields.get(i);
                String mappedColumn = ALLOWED_SORT_COLUMNS.get(field);
                if (mappedColumn == null) {
                    continue;
                }

                String direction = "asc";
                if (sortOrders != null && i < sortOrders.size() && StrUtil.isNotBlank(sortOrders.get(i))) {
                    String normalizedDirection = sortOrders.get(i).trim().toLowerCase(Locale.ROOT);
                    if ("desc".equals(normalizedDirection)) {
                        direction = "desc";
                    }
                }

                safeSortColumns.add(mappedColumn);
                safeSortDirections.add(direction);
            }
        }

        if (safeSortColumns.isEmpty()) {
            safeSortColumns.add(CREATED_MONTH_COLUMN);
            safeSortDirections.add("asc");
        }

        return new ResolvedSort(safeSortColumns, safeSortDirections);
    }

    /**
     * 导出排序解析结果。
     */
    public static final class ResolvedSort {

        private final List<String> columns;
        private final List<String> directions;

        /**
         * 构造解析后的排序结果。
         *
         * @param columns    安全列名
         * @param directions 排序方向
         */
        public ResolvedSort(List<String> columns, List<String> directions) {
            this.columns = columns;
            this.directions = directions;
        }

        /**
         * 获取安全排序列。
         *
         * @return 列名列表
         */
        public List<String> getColumns() {
            return columns;
        }

        /**
         * 获取排序方向。
         *
         * @return 方向列表
         */
        public List<String> getDirections() {
            return directions;
        }
    }
}
