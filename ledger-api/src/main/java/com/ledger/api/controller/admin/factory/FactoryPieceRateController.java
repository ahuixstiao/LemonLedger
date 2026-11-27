package com.ledger.api.controller.admin.factory;

import com.ledger.common.result.Result;
import com.ledger.db.entity.FactoryPieceRate;
import com.ledger.db.service.factory.IFactoryPieceRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.util.StringUtils.hasText;

/**
 * @Author: ahui
 * @Description: TODO 成衣厂计件单价控制器
 * @DateTime: 2025/10/16 - 11:31
 **/
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/admin/factoryPieceRate")
public class FactoryPieceRateController {

    private final IFactoryPieceRateService factoryPieceRateService;

    @Value("${ledger.upload.image-base-dir:../lemonLedgerImages}")
    private String imageBaseDir;


    /**
     * 获取成衣厂计件单价列表
     *
     * @param factoryId   成衣厂ID
     * @param styleNumber 款式编号
     * @param categoryId  工作类型ID
     * @param currentPage 当前页
     * @param pageSize    页面条数
     * @param flag        状态
     * @return result
     */
    @GetMapping
    public Result<Object> queryFactoryPieceRateListByCondition(
            @RequestParam(required = false) String factoryId,
            @RequestParam(required = false) String styleNumber,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false, defaultValue = "1") String currentPage,
            @RequestParam(required = false, defaultValue = "10") String pageSize,
            @RequestParam(required = false, defaultValue = "0") String flag) {

        Integer parsedFactoryId = parseIntegerParam(factoryId);
        Integer parsedCategoryId = parseIntegerParam(categoryId);
        Integer parsedCurrentPage = parseIntegerParam(currentPage, 1);
        Integer parsedPageSize = parseIntegerParam(pageSize, 10);
        Integer parsedFlag = parseIntegerParam(flag, 0);

        return factoryPieceRateService.queryFactoryPieceRateListByCondition(
                parsedFactoryId,
                styleNumber,
                parsedCategoryId,
                parsedCurrentPage,
                parsedPageSize,
                parsedFlag
        );
    }

    /**
     * 按成衣厂ID查询该厂的款式列表（款式编号与样板图片都来自款式表）
     *
     * @param factoryId 工厂ID
     * @param flag      状态
     * @return result
     */
    @GetMapping("/styleNumberList")
    public Result<Object> queryFactoryPieceRateStyleNumberListByFactoryId(
            @RequestParam(required = false) String factoryId,
            @RequestParam(required = false, defaultValue = "0") String flag) {

        Integer parsedFactoryId = parseIntegerParam(factoryId);
        Integer parsedFlag = parseIntegerParam(flag, 0);
        return factoryPieceRateService.queryPieceRateOptionsByFactoryId(parsedFactoryId, parsedFlag);
    }

    /**
     * 保存成衣厂计件单价信息
     *
     * @param factoryPieceRate 计件单价表实体
     * @return result
     */
    @PostMapping
    public Result<Object> saveFactoryPieceRateInfo(@RequestBody FactoryPieceRate factoryPieceRate) {
        return factoryPieceRateService.saveFactoryPieceRateInfo(factoryPieceRate);
    }


    /**
     * 更新成衣厂计件单价信息；单价有变化时按新单价重算指定日期区间内的账单金额
     *
     * @param id               计件单价ID
     * @param factoryPieceRate 计件单价表实体
     * @param startDate        受影响账单的开始日期（改单价时必填）
     * @param endDate          受影响账单的结束日期（改单价时必填）
     * @return result
     */
    @PutMapping("/{id}")
    public Result<Object> updateFactoryPieceRateInfo(@PathVariable Integer id,
                                                    @RequestBody FactoryPieceRate factoryPieceRate,
                                                    @RequestParam(required = false) String startDate,
                                                    @RequestParam(required = false) String endDate) {
        factoryPieceRate.setId(id);
        return factoryPieceRateService.updateFactoryPieceRateInfo(factoryPieceRate, startDate, endDate);
    }


    /**
     * 删除成衣厂计件单价信息
     *
     * @param id 成衣厂计件单价ID
     * @return result
     */
    @DeleteMapping("/{id}")
    public Result<Object> deleteFactoryPieceRateInfo(@PathVariable Integer id) {
        return factoryPieceRateService.deleteFactoryPieceRateById(id);
    }

    private Integer parseIntegerParam(String value) {
        return parseIntegerParam(value, null);
    }

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

}