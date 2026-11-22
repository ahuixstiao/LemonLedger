package com.ledger.common.util;

import java.time.LocalDate;

/**
 * 账单导出文件名：成衣厂-起始年月-截止月日。
 *
 * <p>2 月 27 日～10 月 15 日 → 李波成衣厂-2026年02月-10月15日.xlsx；
 * 截止日期正好是月末时只到月 → 李波成衣厂-2026年02月-10月.xlsx；
 * 没选日期时退回「厂名成衣厂账单.xlsx」。</p>
 *
 * @author ahui
 * @since 2026-11-22
 */
public final class BillFileNameUtil {

    private BillFileNameUtil() {
    }

    /**
     * 生成导出文件名。
     *
     * @param factoryName 成衣厂名称（不含「成衣厂」三个字，为空时用「成衣厂」兜底）
     * @param start       起始日期，可为空
     * @param end         截止日期，可为空
     * @return 形如「李波成衣厂-2026年02月-10月15日.xlsx」
     */
    public static String build(String factoryName, LocalDate start, LocalDate end) {
        String name = factoryName == null || factoryName.isBlank() ? "成衣厂" : factoryName.trim();
        // 兜底名本身就叫「成衣厂」，别再拼一次变成「成衣厂成衣厂」
        String prefix = name.endsWith("成衣厂") ? name : name + "成衣厂";
        if (start == null && end == null) {
            return prefix + "账单.xlsx";
        }

        StringBuilder builder = new StringBuilder(prefix).append('-');
        if (start != null) {
            builder.append(start.getYear()).append('年')
                    .append(twoDigits(start.getMonthValue())).append('月');
        }
        if (end != null) {
            if (start != null) {
                builder.append('-');
            }
            // 跨年时截止日期带上年份，否则只到月
            if (start == null || end.getYear() != start.getYear()) {
                builder.append(end.getYear()).append('年');
            }
            builder.append(twoDigits(end.getMonthValue())).append('月');
            boolean lastDayOfMonth = end.getDayOfMonth() == end.lengthOfMonth();
            if (!lastDayOfMonth) {
                builder.append(twoDigits(end.getDayOfMonth())).append('日');
            }
        }
        return builder.append(".xlsx").toString();
    }

    private static String twoDigits(int value) {
        return value < 10 ? "0" + value : String.valueOf(value);
    }
}
