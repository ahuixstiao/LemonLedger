package com.ledger.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.function.Function;

/**
 * 金额计算工具。
 *
 * <p>规则：金额一律用 {@link BigDecimal}，禁止出现 {@code double}/{@code float}。
 * double 是二进制浮点数，10 个 0.1 相加不等于 1，给客户看的合计行会出现
 * 1234.5599999999999 这种尾巴（Excel 显示时被格式化成 1234.56，但单元格里的真实值不是它，
 * 再拿去做加减就会继续偏）。</p>
 *
 * <p>本类只做「求和 + 保留两位（四舍五入）」这一件事，所有涉及合计的地方都应该走这里，
 * 这样口径只有一处，不会出现「导出合计」和「页面统计」两套算法。</p>
 */
public final class MoneyUtil {

    /**
     * 金额统一保留 2 位小数，与数据库中 decimal(x, 2) 的精度保持一致。
     */
    public static final int SCALE = 2;

    /**
     * 舍入方式：四舍五入。与单条账单计算时使用的 RoundingMode 保持一致。
     */
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private MoneyUtil() {
    }

    /**
     * 对一组金额求和，结果保留两位小数。
     *
     * @param amounts 金额集合，允许为 null；集合中的 null 元素会被跳过
     * @return 合计金额，永远不会返回 null；空集合返回 0.00
     */
    public static BigDecimal sum(Collection<BigDecimal> amounts) {
        if (amounts == null || amounts.isEmpty()) {
            return zero();
        }
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal amount : amounts) {
            if (amount != null) {
                total = total.add(amount);
            }
        }
        return scale(total);
    }

    /**
     * 从一个对象集合中取出金额字段并求和，结果保留两位小数。
     *
     * @param items  数据集合，允许为 null
     * @param getter 取金额的方法，返回 null 时该行按 0 处理
     * @param <T>    数据类型
     * @return 合计金额，永远不会返回 null；空集合返回 0.00
     */
    public static <T> BigDecimal sumBy(Collection<T> items, Function<T, BigDecimal> getter) {
        if (items == null || items.isEmpty()) {
            return zero();
        }
        BigDecimal total = BigDecimal.ZERO;
        for (T item : items) {
            if (item == null) {
                continue;
            }
            BigDecimal amount = getter.apply(item);
            if (amount != null) {
                total = total.add(amount);
            }
        }
        return scale(total);
    }

    /**
     * 把金额规整成两位小数（四舍五入）。
     *
     * @param amount 金额，允许为 null
     * @return 规整后的金额，null 返回 0.00
     */
    public static BigDecimal scale(BigDecimal amount) {
        return (amount == null ? BigDecimal.ZERO : amount).setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * 0.00。
     *
     * @return 两位小数的 0
     */
    public static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE);
    }
}
