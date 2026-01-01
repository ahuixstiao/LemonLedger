package com.ledger.db.entity.dto;

import cn.idev.excel.annotation.ExcelIgnore;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.*;
import cn.idev.excel.enums.poi.HorizontalAlignmentEnum;
import cn.idev.excel.enums.poi.VerticalAlignmentEnum;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @Author: ahui
 * @Description: TODO 工厂账单DTO
 * @DateTime: 2025/10/15 - 23:04
 **/
@HeadRowHeight(50)      // 标题高度
@ColumnWidth(35)        // 列宽
@ContentRowHeight(30)   // 文本行高
// 标题样式 水平和垂直 居中
@HeadStyle(
        horizontalAlignment = HorizontalAlignmentEnum.CENTER,
        verticalAlignment = VerticalAlignmentEnum.CENTER)
// 文本样式 水平和垂直 居中
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER,
        verticalAlignment = VerticalAlignmentEnum.CENTER)
@Data
public class FactoryBillDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -8681847460603512795L;

    @ExcelIgnore
    private Integer id;

    /**
     * 成衣厂ID
     */
    @ExcelIgnore
    private Integer factoryId;
    /**
     * 成衣厂名称
     */
    @ExcelProperty("成衣厂名称")
    private String factoryName;

    /**
     * 床号
     */
    @ExcelProperty("床号")
    private String number;

    /**
     * 款式编号
     */
    @ExcelProperty("款式编号")
    private String styleNumber;

    /**
     * 类型 小花、大花、裤页
     */
    @ExcelIgnore
    private Integer categoryId;

    /**
     * 工作类型名称
     */
    @ExcelIgnore
    private String category;

    /**
     * 单个报价
     */
    @ExcelProperty("单价")
    private BigDecimal quotation;

    /**
     * 数量
     */
    @ExcelProperty("数量")
    private Integer quantity;

    /**
     * 本床账单
     */
    @ExcelProperty("账单")
    private BigDecimal bill;

    /**
     * 创建日期
     */
    @ExcelProperty("日期")
    private LocalDate createdDate;

    @ExcelIgnore
    private Integer flag;


}
