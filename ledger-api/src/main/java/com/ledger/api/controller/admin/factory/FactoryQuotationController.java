package com.ledger.api.controller.admin.factory;

import com.ledger.common.result.Result;
import com.ledger.db.service.factory.IFactoryQuotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: ahui
 * @Description: TODO 成衣厂报价控制器
 * @DateTime: 2025/10/16 - 11:31
 **/
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/admin/factoryQuotation")
public class FactoryQuotationController {

    private final IFactoryQuotationService factoryQuotationService;


    /**
     * 获取成衣厂报价单列表
     *
     * @param factoryId   成衣厂ID
     * @param styleNumber 款式编号
     * @param categoryId  工作类型ID
     * @param currentPage 当前页
     * @param pageSize    页面条数
     * @param flag        状态
     * @return result
     */
    @GetMapping("/list")
    public Result<Object> queryFactoryQuotationListByCondition(
            @RequestParam(required = false) Integer factoryId,
            @RequestParam(required = false) String styleNumber,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false, defaultValue = "1") Integer currentPage,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "0") Integer flag) {


        return factoryQuotationService.queryFactoryQuotationListByCondition(factoryId, styleNumber, categoryId, currentPage, pageSize, flag);
    }

}
