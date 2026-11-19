package com.ledger.db.impl.dashboard;

import com.ledger.common.exception.BizException;
import com.ledger.common.result.Result;
import com.ledger.db.entity.dto.DashboardDailyDTO;
import com.ledger.db.entity.dto.ReconcileRowDTO;
import com.ledger.db.mapper.DashboardMapper;
import com.ledger.db.mapper.FactoryBillMapper;
import com.ledger.db.service.dashboard.IAdminDashboardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.springframework.util.StringUtils.hasText;

/**
 * 后台首页看板实现。
 *
 * <p>口径：账单按 factory_bill.created_date，工资按 job.created_date；
 * 毛利 = 账单金额 - 工资支出；对账口径与「账单与认领对账」页保持一致。</p>
 *
 * @author ahui
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class AdminDashboardServiceImpl implements IAdminDashboardService {

    private final DashboardMapper dashboardMapper;
    private final FactoryBillMapper factoryBillMapper;

    @Override
    public Result<Object> querySummary(String startDate, String endDate) {
        LocalDate today = LocalDate.now();
        LocalDate start = parseDateOrDefault(startDate, today.minusDays(6));
        LocalDate end = parseDateOrDefault(endDate, today);
        if (start.isAfter(end)) {
            throw new BizException("开始日期不能晚于结束日期");
        }
        String startText = start.toString();
        String endText = end.toString();

        Map<String, DashboardDailyDTO> merged = new TreeMap<>();
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            DashboardDailyDTO row = new DashboardDailyDTO();
            row.setDate(day.toString());
            row.setBillQty(BigDecimal.ZERO);
            row.setBillAmount(BigDecimal.ZERO);
            row.setSalary(BigDecimal.ZERO);
            merged.put(day.toString(), row);
        }
        fillBill(dashboardMapper.selectDailyBill(startText, endText), merged);
        fillSalary(dashboardMapper.selectDailySalary(startText, endText), merged);

        BigDecimal billQty = BigDecimal.ZERO;
        BigDecimal billAmount = BigDecimal.ZERO;
        BigDecimal salaryTotal = BigDecimal.ZERO;
        List<Map<String, Object>> daily = new ArrayList<>();
        for (DashboardDailyDTO row : merged.values()) {
            billQty = billQty.add(row.getBillQty());
            billAmount = billAmount.add(row.getBillAmount());
            salaryTotal = salaryTotal.add(row.getSalary());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", row.getDate());
            item.put("billQty", row.getBillQty());
            item.put("billAmount", row.getBillAmount());
            item.put("salary", row.getSalary());
            daily.add(item);
        }

        BigDecimal grossProfit = billAmount.subtract(salaryTotal);
        BigDecimal marginRate = billAmount.compareTo(BigDecimal.ZERO) > 0
                ? grossProfit.multiply(BigDecimal.valueOf(100))
                    .divide(billAmount, 1, RoundingMode.HALF_UP)
                : null;

        List<ReconcileRowDTO> reconcileRows =
                factoryBillMapper.selectReconcileByBatch(null, startText, endText);
        int totalBedCount = reconcileRows.size();
        int diffBedCount = 0;
        BigDecimal claimedQty = BigDecimal.ZERO;
        List<ReconcileRowDTO> topDiff = new ArrayList<>();
        for (ReconcileRowDTO row : reconcileRows) {
            int diff = row.getDiffQty() == null ? 0 : row.getDiffQty();
            claimedQty = claimedQty.add(BigDecimal.valueOf(
                    row.getDoneQty() == null ? 0 : row.getDoneQty()));
            if (diff != 0) {
                diffBedCount++;
                if (topDiff.size() < 5) {
                    topDiff.add(row);
                }
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("startDate", startText);
        data.put("endDate", endText);
        data.put("billQty", billQty);
        data.put("billAmount", billAmount);
        data.put("salaryTotal", salaryTotal);
        data.put("grossProfit", grossProfit);
        data.put("marginRate", marginRate);
        data.put("claimedQty", claimedQty);
        data.put("totalBedCount", totalBedCount);
        data.put("diffBedCount", diffBedCount);
        data.put("daily", daily);
        data.put("topDiff", topDiff);
        return Result.ok(data);
    }

    private void fillBill(List<DashboardDailyDTO> rows, Map<String, DashboardDailyDTO> merged) {
        for (DashboardDailyDTO row : rows) {
            DashboardDailyDTO target = merged.get(row.getDate());
            if (target == null) {
                continue;
            }
            target.setBillQty(nullSafe(row.getBillQty()));
            target.setBillAmount(nullSafe(row.getBillAmount()));
        }
    }

    private void fillSalary(List<DashboardDailyDTO> rows, Map<String, DashboardDailyDTO> merged) {
        for (DashboardDailyDTO row : rows) {
            DashboardDailyDTO target = merged.get(row.getDate());
            if (target == null) {
                continue;
            }
            target.setSalary(nullSafe(row.getSalary()));
        }
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private LocalDate parseDateOrDefault(String value, LocalDate fallback) {
        if (!hasText(value)) {
            return fallback;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception ex) {
            throw new BizException("日期格式不对，应为 yyyy-MM-dd");
        }
    }
}
