package com.ledger.api.controller.admin.settlement;

import cn.idev.excel.FastExcel;
import cn.idev.excel.write.style.HorizontalCellStyleStrategy;
import cn.idev.excel.write.metadata.style.WriteCellStyle;
import cn.idev.excel.write.metadata.style.WriteFont;
import com.ledger.common.exception.BizException;
import com.ledger.common.result.Result;
import com.ledger.db.entity.FactoryBillSettlement;
import com.ledger.db.entity.FactoryBillSettlementItem;
import com.ledger.db.entity.dto.FactoryBillDto;
import com.ledger.db.service.settlement.IFactoryBillSettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 成衣厂账单结算单：导出快照列表、明细、结清登记、按快照重新导出。
 *
 * @author ahui
 * @since 2026-11-22
 */
@RestController
@RequestMapping("/admin/settlement")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AdminSettlementController {

    private final IFactoryBillSettlementService settlementService;

    /**
     * 结算单分页列表。
     */
    @GetMapping
    public Result<Object> list(@RequestParam(required = false) String factoryId,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String startDate,
                               @RequestParam(required = false) String endDate,
                               @RequestParam(defaultValue = "1") Integer currentPage,
                               @RequestParam(defaultValue = "10") Integer pageSize) {
        return settlementService.querySettlementList(
                parseIntegerParam(factoryId),
                parseIntegerParam(status),
                startDate,
                endDate,
                currentPage,
                pageSize);
    }

    /**
     * 结算单明细（导出那一刻每一床的记录）。
     */
    @GetMapping("/{id}/items")
    public Result<Object> items(@PathVariable String id) {
        Integer settlementId = parseIntegerParam(id);
        if (settlementId == null) {
            throw new BizException("结算单ID不对");
        }
        return settlementService.querySettlementItems(settlementId);
    }

    /**
     * 登记结清：记已付金额与付款日期，状态按金额自动算。
     */
    @PutMapping("/{id}/settle")
    public Result<Object> settle(@PathVariable String id, @RequestBody SettlementSettleRequest request) {
        Integer settlementId = parseIntegerParam(id);
        if (settlementId == null) {
            throw new BizException("结算单ID不对");
        }
        LocalDate paidDate = parseDate(request.getPaidDate());
        return settlementService.settle(settlementId, request.getPaidAmount(), paidDate, request.getRemark());
    }

    /**
     * 作废结算单。
     */
    @DeleteMapping("/{id}")
    public Result<Object> voidSettlement(@PathVariable String id) {
        Integer settlementId = parseIntegerParam(id);
        if (settlementId == null) {
            throw new BizException("结算单ID不对");
        }
        return settlementService.voidSettlement(settlementId);
    }

    /**
     * 按快照重新导出这一单的 Excel（内容就是当时导出的那些床）。
     */
    @GetMapping("/{id}/excel")
    public void exportSnapshot(@PathVariable String id, HttpServletResponse response) {
        Integer settlementId = parseIntegerParam(id);
        if (settlementId == null) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return;
        }

        FactoryBillSettlement settlement = settlementService.getSettlement(settlementId);
        if (settlement == null) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }

        List<FactoryBillSettlementItem> items = settlementService.getSettlementItems(settlementId);
        List<FactoryBillDto> rows = new ArrayList<>();
        for (FactoryBillSettlementItem item : items) {
            FactoryBillDto row = new FactoryBillDto();
            row.setFactoryName(settlement.getFactoryName());
            row.setNumber(item.getNumber());
            row.setStyleNumber(item.getStyleNumber());
            row.setCategory(item.getCategory());
            row.setQuantity(item.getQuantity());
            row.setPrice(item.getPrice());
            row.setBill(item.getAmount());
            rows.add(row);
        }

        List<FactoryBillDto> list = new ArrayList<>(rows);
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (FactoryBillDto row : rows) {
            if (row.getBill() != null) {
                totalAmount = totalAmount.add(row.getBill());
            }
        }
        FactoryBillDto summaryRow = new FactoryBillDto();
        summaryRow.setFactoryName("账单总计");
        summaryRow.setBill(totalAmount);
        list.add(new FactoryBillDto());
        list.add(summaryRow);

        String factoryName = settlement.getFactoryName() == null || settlement.getFactoryName().isBlank()
                ? "成衣厂" : settlement.getFactoryName();
        String fileName = factoryName + "成衣厂账单-" + settlement.getId() + ".xlsx";
        String asciiFileName = "factory-bill-settlement-" + settlement.getId() + ".xlsx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + asciiFileName + "\"; filename*=UTF-8''" + encodedFileName);
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");

        WriteFont commonFont = new WriteFont();
        commonFont.setFontHeightInPoints((short) 16);
        WriteCellStyle commonStyle = new WriteCellStyle();
        commonStyle.setWriteFont(commonFont);

        try {
            FastExcel.write(response.getOutputStream(), FactoryBillDto.class)
                    .registerWriteHandler(new HorizontalCellStyleStrategy(commonStyle, commonStyle))
                    .sheet("账单")
                    .doWrite(list);
            response.flushBuffer();
        } catch (IOException e) {
            throw new BizException("按快照导出失败，请稍后重试", e);
        }
    }

    private Integer parseIntegerParam(String value) {
        if (value == null || value.isBlank() || "0".equals(value)) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception e) {
            throw new BizException("日期格式不对，应为 yyyy-MM-dd");
        }
    }

    /**
     * 结清登记请求体。
     */
    @lombok.Data
    public static class SettlementSettleRequest {
        /** 已付金额（累计），为空表示不改 */
        private BigDecimal paidAmount;
        /** 付款日期 yyyy-MM-dd */
        private String paidDate;
        /** 备注 */
        private String remark;
    }
}
