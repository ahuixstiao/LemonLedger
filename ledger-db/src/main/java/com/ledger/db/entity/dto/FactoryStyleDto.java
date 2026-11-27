package com.ledger.db.entity.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 成衣厂款式列表/下拉用的传输对象
 * </p>
 *
 * @author ahui
 * @since 2026-11-27
 */
@Data
public class FactoryStyleDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -3164987520456317720L;

    /**
     * 款式ID
     */
    private Integer id;

    /**
     * 成衣厂ID
     */
    private Integer factoryId;

    /**
     * 成衣厂名称
     */
    private String factoryName;

    /**
     * 款式编号
     */
    private String styleNumber;

    /**
     * 样板图片访问路径
     */
    private String imagePath;

    /**
     * 该款式下配置了多少条计件单价
     */
    private Integer rateCount;

    /**
     * 已配价工种的摘要，形如「后袋 ¥0.18 / 裤页 ¥0.35」
     */
    private String rateSummary;

    /**
     * 创建日期
     */
    private String createdDate;

    /**
     * 删除状态 0正常 1删除
     */
    private Integer flag;
}
