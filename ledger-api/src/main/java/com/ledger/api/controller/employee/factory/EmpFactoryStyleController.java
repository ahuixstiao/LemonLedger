package com.ledger.api.controller.employee.factory;

import com.ledger.common.result.Result;
import com.ledger.db.service.factory.IFactoryStyleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: ahui
 * @Description: 员工端样板图片列表控制器（数据来自成衣厂款式表）
 * @DateTime: 2026/11/27
 **/
@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/emp/factoryStyle")
public class EmpFactoryStyleController {

    private final IFactoryStyleService factoryStyleService;

    /**
     * 员工端样板图片列表：按成衣厂和款式编号筛选，只返回已经有图片的款式。
     *
     * @param factoryId   成衣厂ID（可为空，空则查全部成衣厂）
     * @param styleNumber 款式编号（可为空，支持模糊匹配）
     * @param currentPage 当前页，默认 1
     * @param pageSize    每页条数，默认 12
     * @return result，data 里是 records/total/current/size/pages
     */
    @GetMapping("/sampleList")
    public Result<Object> querySampleStyleList(
            @RequestParam(required = false) String factoryId,
            @RequestParam(required = false) String styleNumber,
            @RequestParam(required = false, defaultValue = "1") String currentPage,
            @RequestParam(required = false, defaultValue = "12") String pageSize) {

        Integer parsedFactoryId = parseIntegerParam(factoryId);
        Integer parsedPage = parseIntegerParam(currentPage);
        Integer parsedSize = parseIntegerParam(pageSize);
        String normalizedStyleNumber = StringUtils.hasText(styleNumber) ? styleNumber.trim() : null;
        log.info("员工端样板图片列表：factoryId={}, styleNumber={}, currentPage={}, pageSize={}",
                parsedFactoryId, normalizedStyleNumber, parsedPage, parsedSize);
        return factoryStyleService.querySampleStyleList(parsedFactoryId, normalizedStyleNumber, parsedPage, parsedSize);
    }

    /**
     * 将字符串参数解析为整数，空值或非法值返回 null。
     *
     * @param value 参数值
     * @return 解析后的整数，或 null
     */
    private Integer parseIntegerParam(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
