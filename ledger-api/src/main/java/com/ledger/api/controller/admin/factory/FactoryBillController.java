package com.ledger.api.controller.admin.factory;

import cn.idev.excel.FastExcel;
import cn.idev.excel.write.metadata.style.WriteCellStyle;
import cn.idev.excel.write.metadata.style.WriteFont;
import cn.idev.excel.write.style.HorizontalCellStyleStrategy;

import com.ledger.common.exception.BizException;
import com.ledger.common.result.Result;
import com.ledger.common.util.MoneyUtil;
import com.ledger.db.entity.FactoryBill;
import com.ledger.db.entity.dto.FactoryBillDto;
import com.ledger.db.service.factory.IFactoryBillService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.hasText;

/**
 * @Author: ahui
 * @Description: 成衣厂账单控制器
 * @DateTime: 2025/10/16 - 11:33
 **/
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/admin/factoryBill")
@Validated
@Slf4j
public class FactoryBillController {

    private final IFactoryBillService factoryBillService;

    /**
     * 按条件查询成衣厂账单列表  如果不传递参数则默认查询全部成衣厂账单列表
     *
     * @param factoryId   工厂ID
     * @param number      床号
     * @param styleNumber 款式编号（计件单价表）
     * @param flag        删除状态 0否 1是
     * @return result
     */
    @GetMapping
    public Result<Object> queryFactoryBillListByCondition(
            @RequestParam(required = false) Integer factoryId,
            @RequestParam(required = false) String number,
            @RequestParam(required = false) String styleNumber,
            @RequestParam(required = false, defaultValue = "0") Integer flag,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "1") Integer currentPage,
            @RequestParam(required = false, defaultValue = "5") Integer pageSize

    ) {
        return factoryBillService.queryFactoryBillListByCondition(
                factoryId, number, styleNumber, flag,
                startDate, endDate, currentPage, pageSize);
    }

    /**
     * 统计成衣厂账单。
     * 后续导出明细时，日期排序按月份聚合，不再按具体日期排序。
     *
     * @param factoryId 成衣厂ID
     * @param startDate 起止日期
     * @param endDate   起止日期
     * @param flag      状态
     * @return result
     */
    @GetMapping("/bill/{factoryId}")
    public Result<Object> statisticalFactoryBill(
            @PathVariable @NotNull String factoryId,
            @RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "0") String flag) {

        Integer parsedFactoryId = parseIntegerParam(factoryId);
        Integer parsedFlag = parseIntegerParam(flag, 0);

        if (parsedFactoryId == null) {
            return Result.fail("工厂ID格式错误");
        }

        return factoryBillService.statisticalBill(parsedFactoryId, startDate, endDate, parsedFlag);
    }

    /**
     * 按条件导出成衣厂账单 Excel。
     * 未指定排序字段时默认按账单月份升序；选择创建日期时同样按月份排序。
     *
     * @param factoryId 成衣厂ID
     * @param startDate 起始日期
     * @param endDate   截止日期
     */
    @GetMapping("/excel/{factoryId}")
    public void exportFactoryBillExcelByCondition(
            HttpServletResponse response,
            @PathVariable @NotNull String factoryId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String sortFields,
            @RequestParam(required = false) String sortOrders
    ) {
        Integer parsedFactoryId = parseIntegerParam(factoryId);
        if (parsedFactoryId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        List<String> parsedSortFields = splitCsv(sortFields);
        List<String> parsedSortOrders = splitCsv(sortOrders);

        // 查询数据
        List<FactoryBillDto> queryResult = factoryBillService.exportFactoryBillExcelByCondition(
                parsedFactoryId,
                startDate,
                endDate,
                parsedSortFields,
                parsedSortOrders
        );

        // 查询结果可能是只读列表，复制一份再追加"空行 + 总计行"
        List<FactoryBillDto> list = new ArrayList<>(queryResult);

        // 文件名要在追加统计行之前取，否则第一行会被空行/总计行占掉
        String factoryName = queryResult.stream()
                .map(FactoryBillDto::getFactoryName)
                .filter(name -> hasText(name))
                .findFirst()
                .orElse("成衣厂");
        String fileName = factoryName + "成衣厂账单.xlsx";

        // 获取总计：BigDecimal 相加，不能用 double（二进制浮点会让总计出现 1234.5599999999999 这种尾巴）
        BigDecimal totalAmount = MoneyUtil.sumBy(list, FactoryBillDto::getBill);

        // 创建一个特殊的"总计行"对象
        FactoryBillDto summaryRow = new FactoryBillDto();
        summaryRow.setFactoryName("账单总计");
        summaryRow.setBill(totalAmount);
        // 添加一个空行作为间隔
        list.add(new FactoryBillDto());
        // 将总计行添加到列表末尾
        list.add(summaryRow);

        // 格式化文件名，兼容 MacOS/Windows 下载行为
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        String contentDisposition = "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName;

        // 设置响应内容文件类型
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // 设为输出 excel文件
        response.setCharacterEncoding("utf-8");
        // 添加响应头信息
        response.setHeader("Content-Disposition", contentDisposition); // 设置头
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // 不缓存
        response.setHeader("Pragma", "no-cache");

        // 构建Excel字体样式对象
        WriteFont commonFont = new WriteFont();
        // 配置字体大小
        commonFont.setFontHeightInPoints((short) 16);
        // 创建单元格样式，并关联字体
        WriteCellStyle commonStyle = new WriteCellStyle();
        commonStyle.setWriteFont(commonFont);


        try {
            log.info("开始导出成衣厂账单 Excel，工厂: {}，数据量: {}", factoryName, list.size());
            FastExcel
                    .write(response.getOutputStream(), FactoryBillDto.class)
                    .registerWriteHandler(new HorizontalCellStyleStrategy(commonStyle, commonStyle))
                    .sheet("账单")
                    .doWrite(list);
            response.flushBuffer();
        } catch (IOException ioException) {
            // 带上堆栈再往外抛：原来的 log.error(getMessage()) 只留一行文字，出问题无从查起
            log.error("导出成衣厂账单 Excel 失败，工厂ID: {}", parsedFactoryId, ioException);
            throw new BizException("导出 Excel 失败，请稍后重试", ioException);
        }
    }

    /**
     * 保存成衣厂账单信息
     *
     * @param bill 成衣厂账单实体
     * @return result
     */
    @PostMapping
    public Result<Object> saveFactoryBillInfo(
            @RequestBody FactoryBill bill,
            @RequestParam(required = false, defaultValue = "false") boolean confirmDuplicate) {
        return factoryBillService.saveFactoryBillInfo(bill, confirmDuplicate);
    }

    /**
     * 更新成衣厂账单信息
     *
     * @param bill 成衣厂账单实体
     * @return result
     */
    @PutMapping("/{id}")
    public Result<Object> updateFactoryBillInfo(
            @PathVariable Integer id,
            @RequestBody FactoryBill bill,
            @RequestParam(required = false, defaultValue = "false") boolean confirmDuplicate) {
        bill.setId(id);
        return factoryBillService.updateFactoryBillInfo(bill, confirmDuplicate);
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

    /**
     * 按成衣厂统计毛利（账单收入 - 工资支出）。
     *
     * <p>两个日期都不传时按「本月 1 号到今天」。
     * 结果按毛利从高到低排，前端直接渲染成一张表。</p>
     *
     * @param startDate 起始日期 yyyy-MM-dd，可为空
     * @param endDate   结束日期 yyyy-MM-dd，可为空
     * @return result
     */
    @GetMapping("/gross")
    public Result<Object> queryGrossProfit(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return factoryBillService.queryGrossProfit(startDate, endDate);
    }

    /**
     * 将请求参数解析为 Integer，解析失败时返回 null。
     *
     * @param value 原始参数
     * @return 解析后的整数
     */
    private Integer parseIntegerParam(String value) {
        return parseIntegerParam(value, null);
    }

    /**
     * 将请求参数解析为 Integer，空值或非法值时返回默认值。
     *
     * @param value        原始参数
     * @param defaultValue 默认值
     * @return 解析后的整数
     */
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

    /**
     * 将逗号分隔字符串拆分为去空白后的列表。
     *
     * @param csv 逗号分隔字符串
     * @return 拆分后的列表
     */
    private List<String> splitCsv(String csv) {
        if (!hasText(csv)) {
            return Collections.emptyList();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toList());
    }

}
