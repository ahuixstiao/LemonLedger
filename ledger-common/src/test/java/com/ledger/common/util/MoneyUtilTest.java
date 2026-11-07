package com.ledger.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * MoneyUtil 的单元测试。
 *
 * <p>重点不是"能算对 1+1"，而是钉住两件事：
 * 一是合计永远保留两位小数；二是空集合 / null 元素不会让调用方拿到 null 或 NPE。</p>
 */
class MoneyUtilTest {

    @Test
    @DisplayName("空集合返回 0.00，而不是 null")
    void sum_emptyCollection_returnsZero() {
        BigDecimal total = MoneyUtil.sum(Collections.emptyList());

        assertNotNull(total);
        assertEquals(new BigDecimal("0.00"), total);
        assertEquals(2, total.scale());
    }

    @Test
    @DisplayName("集合本身为 null 时也不抛异常")
    void sum_nullCollection_returnsZero() {
        assertEquals(new BigDecimal("0.00"), MoneyUtil.sum(null));
    }

    @Test
    @DisplayName("元素里有 null 时跳过，不影响其它金额")
    void sum_withNullElement_skipsIt() {
        List<BigDecimal> amounts = Arrays.asList(new BigDecimal("12.30"), null, new BigDecimal("7.70"));

        assertEquals(new BigDecimal("20.00"), MoneyUtil.sum(amounts));
    }

    @Test
    @DisplayName("用 double 会算错的这组数，用 BigDecimal 合计必须精确")
    void sum_avoidsDoublePrecisionProblem() {
        // 0.1 在 double 里存不下，100 个 0.1 相加会得到 9.999999999999831
        List<BigDecimal> amounts = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            amounts.add(new BigDecimal("0.10"));
        }

        assertEquals(new BigDecimal("10.00"), MoneyUtil.sum(amounts));

        // 顺带证明一下 double 确实不行，说明这个工具类不是多余的
        double naive = 0.0;
        for (int i = 0; i < 100; i++) {
            naive += 0.1;
        }
        assertEquals(10.0, naive, 1e-9);
        assertEquals(false, naive == 10.0);
    }

    @Test
    @DisplayName("合计结果固定两位小数")
    void sum_alwaysScalesToTwoDecimals() {
        List<BigDecimal> amounts = Arrays.asList(new BigDecimal("1"), new BigDecimal("2"));

        BigDecimal total = MoneyUtil.sum(amounts);

        assertEquals(new BigDecimal("3.00"), total);
        assertEquals(2, total.scale());
    }

    @Test
    @DisplayName("按字段求和：跳过 null 行和 null 金额")
    void sumBy_skipsNullRowsAndNullAmounts() {
        List<Row> rows = Arrays.asList(
                new Row(new BigDecimal("99.90")),
                null,
                new Row(null),
                new Row(new BigDecimal("0.10"))
        );

        assertEquals(new BigDecimal("100.00"), MoneyUtil.sumBy(rows, Row::getAmount));
    }

    @Test
    @DisplayName("scale：四舍五入到两位，null 当 0")
    void scale_roundsHalfUp() {
        assertEquals(new BigDecimal("1.01"), MoneyUtil.scale(new BigDecimal("1.005")));
        assertEquals(new BigDecimal("1.00"), MoneyUtil.scale(new BigDecimal("1.004")));
        assertEquals(new BigDecimal("0.00"), MoneyUtil.scale(null));
    }

    @Test
    @DisplayName("单条账单金额：数量 × 单价，两位四舍五入（与业务代码口径一致）")
    void scale_matchesBillCalculation() {
        // 127 件 × 7.35 元 = 933.45
        BigDecimal bill = MoneyUtil.scale(BigDecimal.valueOf(127).multiply(new BigDecimal("7.35")));

        assertEquals(new BigDecimal("933.45"), bill);
    }

    /**
     * 测试用的最小行对象。
     */
    private static final class Row {

        private final BigDecimal amount;

        private Row(BigDecimal amount) {
            this.amount = amount;
        }

        private BigDecimal getAmount() {
            return amount;
        }
    }
}
