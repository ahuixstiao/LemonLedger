package com.ledger.api.controller.admin.factory;

import com.ledger.common.result.Result;
import com.ledger.db.entity.FactoryQuotation;
import com.ledger.db.service.factory.IFactoryQuotationService;
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
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.util.StringUtils.hasText;

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

    @Value("${ledger.upload.image-base-dir:../lemonLedgerImages}")
    private String imageBaseDir;


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
    @GetMapping
    public Result<Object> queryFactoryQuotationListByCondition(
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

        return factoryQuotationService.queryFactoryQuotationListByCondition(
                parsedFactoryId,
                styleNumber,
                parsedCategoryId,
                parsedCurrentPage,
                parsedPageSize,
                parsedFlag
        );
    }

    /**
     * 按成衣厂ID获取对应工厂的报价单款式编号列表
     *
     * @param factoryId 工厂ID
     * @param flag      状态
     * @return result
     */
    @GetMapping("/styleNumberList")
    public Result<Object> queryFactoryQuotationStyleNumberListByFactoryId(
            @RequestParam(required = false) String factoryId,
            @RequestParam(required = false, defaultValue = "0") String flag) {

        Integer parsedFactoryId = parseIntegerParam(factoryId);
        Integer parsedFlag = parseIntegerParam(flag, 0);
        return factoryQuotationService.queryStyleNumberListByFactoryId(parsedFactoryId, parsedFlag);
    }


    /**
     * 保存成衣厂报价信息
     *
     * @param factoryQuotation 报价表实体
     * @return result
     */
    @PostMapping
    public Result<Object> saveFactoryQuotationInfo(@RequestBody FactoryQuotation factoryQuotation) {
        return factoryQuotationService.saveFactoryQuotationInfo(factoryQuotation);
    }


    /**
     * 更新成衣厂报价信息
     *
     * @param factoryQuotation 报价表实体
     * @return result
     */
    @PutMapping("/{id}")
    public Result<Object> updateFactoryQuotationInfo(@PathVariable Integer id, @RequestBody FactoryQuotation factoryQuotation) {
        factoryQuotation.setId(id);
        return factoryQuotationService.updateFactoryQuotationInfo(factoryQuotation);
    }


    /**
     * 上传成衣厂报价图片
     *
     * @param id   成衣厂报价ID
     * @param file 图片文件
     * @return result
     */
    @PostMapping("/{id}/image")
    public Result<Object> uploadFactoryQuotationImage(@PathVariable Integer id,
                                                      @RequestParam("file") MultipartFile file) {
        return factoryQuotationService.uploadFactoryQuotationImage(id, file);
    }

    /**
     * 删除成衣厂报价图片（删除磁盘文件并清空数据库路径）。
     *
     * @param id 成衣厂报价ID
     * @return result
     */
    @DeleteMapping("/{id}/image")
    public Result<Object> deleteFactoryQuotationImage(@PathVariable Integer id) {
        return factoryQuotationService.deleteFactoryQuotationImage(id);
    }

    /**
     * 读取报价图片文件，兜底处理静态资源映射失效场景。
     *
     * @param request 相对图片路径
     * @return 图片文件响应
     */
    @GetMapping("/imageView/**")
    public ResponseEntity<Resource> viewFactoryQuotationImage(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String prefix = "/admin/factoryQuotation/imageView/";
        int prefixIndex = uri.indexOf(prefix);
        if (prefixIndex < 0) {
            return ResponseEntity.notFound().build();
        }

        String relativePath = uri.substring(prefixIndex + prefix.length());
        Path basePath = Paths.get(imageBaseDir).toAbsolutePath().normalize();
        Path targetPath = basePath.resolve(relativePath).normalize();

        if (!targetPath.startsWith(basePath)) {
            return ResponseEntity.notFound().build();
        }

        try {
            Resource resource = new UrlResource(targetPath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            MediaType mediaType = MediaTypeFactory
                    .getMediaType(resource)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
                    .body(resource);
        } catch (Exception exception) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 删除成衣厂报价信息
     *
     * @param id 成衣厂报价ID
     * @return result
     */
    @DeleteMapping("/{id}")
    public Result<Object> deleteFactoryQuotationInfo(@PathVariable Integer id) {
        return factoryQuotationService.deleteFactoryQuotationById(id);
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