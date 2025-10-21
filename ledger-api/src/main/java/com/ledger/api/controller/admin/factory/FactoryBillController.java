package com.ledger.api.controller.admin.factory;

import com.ledger.common.result.Result;
import com.ledger.db.service.factory.IFactoryBillService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: ahui
 * @Description: TODO 成衣厂账单控制器
 * @DateTime: 2025/10/16 - 11:33
 **/
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/factoryBill")
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
    @GetMapping("/list/{factoryId}")
    public Result<Object> queryFactoryBillListByCondition(
            @PathVariable(required = false) Integer factoryId,
            @RequestParam(required = false) Integer number,
            @RequestParam(required = false) Integer styleNumber,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false, defaultValue = "0") Integer flag
    ) {
        return factoryBillService.queryFactoryBillListByCondition(factoryId, number, styleNumber, categoryId, flag);
    }

}
