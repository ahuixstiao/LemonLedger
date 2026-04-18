package com.ledger.api.controller.employee.factory;

import com.ledger.common.result.Result;
import com.ledger.db.service.factory.IFactoryQuotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: ahui
 * @Description: 员工端成衣厂样板查询控制器
 * @DateTime: 2026/3/7
 **/
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/emp/factoryQuotation")
public class EmpFactoryQuotationController {

    private final IFactoryQuotationService factoryQuotationService;

    /**
     * 员工端：按款式编号查询样板图片列表，仅返回款式编号和图片路径。
     *
     * @param styleNumber 款式编号（可为空，支持模糊匹配）
     * @param flag        删除状态，默认0（仅正常数据）
     * @return result
     */
    @GetMapping("/sampleList")
    public Result<Object> querySampleListByStyleNumber(
            @RequestParam(required = false) String styleNumber,
            @RequestParam(required = false, defaultValue = "0") String flag) {

        Integer parsedFlag = parseIntegerParam(flag, 0);
        String normalizedStyleNumber = StringUtils.hasText(styleNumber) ? styleNumber.trim() : null;
        return factoryQuotationService.querySampleListByStyleNumber(normalizedStyleNumber, parsedFlag);
    }

    /**
     * 将字符串参数解析为整数，解析失败时返回默认值。
     *
     * @param value        参数值
     * @param defaultValue 默认值
     * @return 解析后的整数
     */
    private Integer parseIntegerParam(String value, Integer defaultValue) {
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }
}
