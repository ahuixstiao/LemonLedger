package com.ledger.api.controller.admin.batch;

import com.ledger.common.result.Result;
import com.ledger.db.service.IBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 床管理接口
 * </p>
 * <p>
 * 床不是让人手工维护的表：工单、账单、工作记录录入时按 (厂, 床号, 款式) 自动建档。
 * 这里只提供两个能力——查一查现在有哪些床、以及删掉误建的床（被单据引用时会被拒绝）。
 * </p>
 *
 * @author ahui
 * @since 2026-11-06
 */
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/admin/batch")
public class BatchController {

    private final IBatchService batchService;

    /**
     * 分页查询床列表
     *
     * @param factoryId   成衣厂ID（可选）
     * @param number      床号（可选，模糊）
     * @param styleNumber 款式编号（可选，模糊）
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @return result
     */
    @GetMapping
    public Result<Object> queryBatchList(
            @RequestParam(required = false) Integer factoryId,
            @RequestParam(required = false) String number,
            @RequestParam(required = false) String styleNumber,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        return batchService.queryBatchList(factoryId, number, styleNumber, currentPage, pageSize);
    }

    /**
     * 删除误建的床（逻辑删除）
     *
     * @param id 床ID
     * @return result
     */
    @DeleteMapping("/{id}")
    public Result<Object> deleteBatch(@PathVariable Integer id) {
        return batchService.deleteBatch(id);
    }
}
