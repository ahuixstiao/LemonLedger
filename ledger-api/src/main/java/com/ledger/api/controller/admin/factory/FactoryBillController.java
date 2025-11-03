package com.ledger.api.controller.admin.factory;

import com.ledger.common.result.Result;
import com.ledger.db.entity.FactoryBill;
import com.ledger.db.service.factory.IFactoryBillService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

/**
 * @Author: ahui
 * @Description: TODO 成衣厂账单控制器
 * @DateTime: 2025/10/16 - 11:33
 **/
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/admin/factoryBill")
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
    @GetMapping("/list")
    public Result<Object> queryFactoryBillListByCondition(
            @RequestParam(required = false) Integer factoryId,
            @RequestParam(required = false) String number,
            @RequestParam(required = false) String styleNumber,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false, defaultValue = "0") Integer flag,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "1") Integer currentPage,
            @RequestParam(required = false, defaultValue = "5") Integer pageSize

    ) {
        return factoryBillService.queryFactoryBillListByCondition(
                factoryId, number, styleNumber, categoryId, flag,
                startDate, endDate, currentPage, pageSize);
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
     * 保存成衣厂账单信息
     *
     * @param bill 成衣厂账单实体
     * @return result
     */
    @PostMapping("/save")
    public Result<Object> saveFactoryBillInfo(@RequestBody FactoryBill bill) {

        return factoryBillService.saveFactoryBillInfo(bill);
    }

    /**
     * 删除成衣厂账单信息
     *
     * @param factoryBillId 账单ID
     * @return result
     */
    @DeleteMapping("/delete/{factoryBillId}")
    public Result<Object> deleteFactoryBillInfo(@PathVariable @NotNull Integer factoryBillId) {

        return factoryBillService.deleteFactoryBillInfo(factoryBillId);
    }

}
