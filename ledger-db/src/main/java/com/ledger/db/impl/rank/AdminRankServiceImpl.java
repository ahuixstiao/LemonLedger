package com.ledger.db.impl.rank;

import com.ledger.common.exception.BizException;
import com.ledger.common.result.Result;
import com.ledger.db.entity.dto.EmployeeRankDTO;
import com.ledger.db.mapper.JobMapper;
import com.ledger.db.service.rank.IAdminRankService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.springframework.util.StringUtils.hasText;

/**
 * 员工产能排行实现。
 *
 * <p>件数、工资、出勤天数都在 SQL 里按员工汇总；
 * 平均单价（元/件）在这里算，顺便补上总计，页面直接渲染即可。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AdminRankServiceImpl implements IAdminRankService {

    private final JobMapper jobMapper;

    @Override
    public Result<Object> queryEmployeeRank(Integer employeeId, String startDate, String endDate) {
        LocalDate today = LocalDate.now();
        LocalDate start = LocalDate.parse(Objects.requireNonNullElse(
                normalizeDateParam(startDate), today.minusDays(29).toString()));
        LocalDate end = LocalDate.parse(Objects.requireNonNullElse(
                normalizeDateParam(endDate), today.toString()));
        if (start.isAfter(end)) {
            throw new BizException("开始日期不能晚于结束日期");
        }

        List<EmployeeRankDTO> rows = jobMapper.selectEmployeeRank(employeeId, start.toString(), end.toString());

        BigDecimal totalSalary = BigDecimal.ZERO;
        long totalQuantity = 0;
        for (EmployeeRankDTO row : rows) {
            totalQuantity += Objects.requireNonNullElse(row.getQuantity(), 0);
            totalSalary = totalSalary.add(Objects.requireNonNullElse(row.getSalary(), BigDecimal.ZERO));
            // 平均单价：件数为 0 时留空，前端显示「–」
            row.setAvgPrice(row.getQuantity() != null && row.getQuantity() > 0
                    ? row.getSalary().divide(BigDecimal.valueOf(row.getQuantity()), 2, RoundingMode.HALF_UP)
                    : null);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("startDate", start.toString());
        result.put("endDate", end.toString());
        result.put("people", rows.size());
        result.put("totalQuantity", totalQuantity);
        result.put("totalSalary", totalSalary);
        result.put("rows", rows);
        return Result.ok(result);
    }

    /**
     * 规整日期参数：空值/空串/"0" 视为未传，非法日期也视为未传。
     */
    private String normalizeDateParam(String dateParam) {
        if (!hasText(dateParam)) {
            return null;
        }
        String value = dateParam.trim();
        if ("0".equals(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value).format(DateTimeFormatter.ISO_DATE);
        } catch (Exception exception) {
            return null;
        }
    }
}
