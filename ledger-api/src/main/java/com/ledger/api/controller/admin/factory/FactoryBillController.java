package com.ledger.api.controller.admin.factory;

import cn.idev.excel.FastExcel;
import cn.idev.excel.write.metadata.style.WriteCellStyle;
import cn.idev.excel.write.metadata.style.WriteFont;
import cn.idev.excel.write.style.HorizontalCellStyleStrategy;

import com.ledger.common.result.Result;
import com.ledger.db.entity.FactoryBill;
import com.ledger.db.entity.dto.FactoryBillDto;
import com.ledger.db.service.factory.IFactoryBillService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static org.springframework.util.StringUtils.hasText;

import com.sun.istack.NotNull;

/**
 * @Author: ahui
 * @Description: TODO 成衣厂账单控制器
 * @DateTime: 2025/10/16 - 11:33
 **/
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/admin/factoryBill")
@Slf4j
public class FactoryBillController {

    private final IFactoryBillService factoryBillService;

    /**
     * 按条件查询成衣厂账单列表  如果不传递参数则默认查询全部成衣厂账单列表
     *
     * @param factoryId   工厂ID
     * @param number      床号
     * @param styleNumber 款式编号
     * @param categoryId  工作类型
     * @param flag        删除状态 0否 1是
     * @return result
     */
    @GetMapping
    public Result<Object> queryFactoryBillListByCondition(
            @RequestParam(required = false) String factoryId,
            @RequestParam(required = false) String number,
            @RequestParam(required = false) String styleNumber,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false, defaultValue = "0") String flag,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "1") String currentPage,
            @RequestParam(required = false, defaultValue = "5") String pageSize

    ) {
        Integer parsedFactoryId = parseIntegerParam(factoryId);
        Integer parsedCategoryId = parseIntegerParam(categoryId);
        Integer parsedFlag = parseIntegerParam(flag, 0);
        Integer parsedCurrentPage = parseIntegerParam(currentPage, 1);
        Integer parsedPageSize = parseIntegerParam(pageSize, 5);

        return factoryBillService.queryFactoryBillListByCondition(
                parsedFactoryId, number, styleNumber, parsedCategoryId, parsedFlag,
                startDate, endDate, parsedCurrentPage, parsedPageSize);
    }

    /**
     * 统计成衣厂账单
     *
     * @param factoryId 成衣厂ID
     * @param startDate 起止日期
     * @param endDate   起止日期
     * @param flag      状态
     * @return result
     */
    @GetMapping("/bill/{factoryId}")
    public Result<Object> statisticalFactoryBill(
            @PathVariable @NotNull Integer factoryId,
            @RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "0") Integer flag) {

        return factoryBillService.statisticalBill(factoryId, startDate, endDate, flag);
    }

    /**
     * 按条件导出成衣厂账单 Excel
     *
     * @param factoryId 成衣厂ID
     * @param startDate 起始日期
     * @param endDate   截止日期
     */
    @GetMapping("/excel/{factoryId}")
    public void exportFactoryBillExcelByCondition(
            HttpServletResponse response,
            @PathVariable @NotNull Integer factoryId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        // 查询数据
        List<FactoryBillDto> list = factoryBillService.exportFactoryBillExcelByCondition(factoryId, startDate, endDate);

        // 获取总计
        double totalAmount = list.stream()
                .map(FactoryBillDto::getBill)      // 1. 提取金额字段（BigDecimal 类型）
                .filter(Objects::nonNull)          // 2. 过滤掉可能的 null 值，避免空指针
                .mapToDouble(BigDecimal::doubleValue) // 3. 转换为 double 流以便求和
                .sum();

        // 创建一个特殊的“总计行”对象，其字段根据需要设置
        FactoryBillDto summaryRow = new FactoryBillDto();
        // 设置一个特殊行的标识，例如工厂名称为“账单总计”
        summaryRow.setFactoryName("账单总计");
        // 其他文本列可以留空或设为你需要的提示
        summaryRow.setBill(BigDecimal.valueOf(totalAmount)); // 在金额列放入总计
        // 添加一个空行作为间隔
        list.add(new FactoryBillDto());
        // 将总计行添加到列表末尾
        list.add(summaryRow);

        // 构建文件名
        String fileName = list.get(0).getFactoryName() + "成衣厂账单.xlsx";

        // 格式化文件名，处理中文乱码问题
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");;

        // TODO 构建文件流供浏览器下载

        // 设置响应内容文件类型
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // 设为输出 excel文件
        response.setCharacterEncoding("utf-8");
        // 添加响应头信息
        response.setHeader("Content-disposition", "attachment;filename=" + encodedFileName); // 设置头
        response.setHeader("Cache-Control", "No-cache"); // 不缓存
        response.setHeader("Pragma", "No-cache");

        // 构建Excel字体样式对象
        WriteFont commonFont = new WriteFont();
        // 配置字体大小
        commonFont.setFontHeightInPoints((short) 16);
        // 创建单元格样式，并关联字体
        WriteCellStyle commonStyle = new WriteCellStyle();
        commonStyle.setWriteFont(commonFont);


        try {
            // TODO 写入本地
            log.info("即将导出的数据: {}", list);
            log.info("开始导出成衣厂账单 Excel 数据量: {}", list.size());
            log.info("文件名: {}", encodedFileName);
            log.info("响应状态: {}", response.getStatus());
            FastExcel
                    .write(response.getOutputStream(), FactoryBillDto.class)
                    .registerWriteHandler(new HorizontalCellStyleStrategy(commonStyle, commonStyle))
                    .sheet("账单")
                    .doWrite(list);
        } catch (IOException ioException) {
            // 打印异常信息
            log.error(ioException.getMessage());
        }
    }

    /**
     * 保存成衣厂账单信息
     *
     * @param bill 成衣厂账单实体
     * @return result
     */
    @PostMapping
    public Result<Object> saveFactoryBillInfo(@RequestBody FactoryBill bill) {
        return factoryBillService.saveFactoryBillInfo(bill);
    }

    /**
     * 更新成衣厂账单信息
     *
     * @param bill 成衣厂账单实体
     * @return result
     */
    @PutMapping("/{id}")
    public Result<Object> updateFactoryBillInfo(@PathVariable Integer id, @RequestBody FactoryBill bill) {
        bill.setId(id);
        return factoryBillService.updateFactoryBillInfo(bill);
    }

    /**
     * 删除成衣厂账单信息
     *
     * @param id 账单ID
     * @return result
     */
    @DeleteMapping("/{id}")
    public Result<Object> deleteFactoryBillInfo(@PathVariable @NotNull Integer id) {
        return factoryBillService.deleteFactoryBillInfo(id);
    }

    private Integer parseIntegerParam(String value) {
        return parseIntegerParam(value, null);
    }

    private Integer parseIntegerParam(String value, Integer defaultValue) {
        if (!hasText(value)) {
            return defaultValue;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

}
