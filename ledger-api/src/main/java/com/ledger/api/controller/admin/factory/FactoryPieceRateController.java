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
import org.springframework.web.multipart.MultipartFile;

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
     * 按成衣厂ID获取对应工厂的计件单价单款式编号列表
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
        return factoryPieceRateService.queryStyleNumberListByFactoryId(parsedFactoryId, parsedFlag);
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
     * 更新成衣厂计件单价信息
     *
     * @param factoryPieceRate 计件单价表实体
     * @return result
     */
    @PutMapping("/{id}")
    public Result<Object> updateFactoryPieceRateInfo(@PathVariable Integer id, @RequestBody FactoryPieceRate factoryPieceRate) {
        factoryPieceRate.setId(id);
        return factoryPieceRateService.updateFactoryPieceRateInfo(factoryPieceRate);
    }


    /**
     * 上传成衣厂计件单价图片
     *
     * @param id   成衣厂计件单价ID
     * @param file 图片文件
     * @return result
     */
    @PostMapping("/{id}/image")
    public Result<Object> uploadFactoryPieceRateImage(@PathVariable Integer id,
                                                      @RequestParam("file") MultipartFile file) {
        return factoryPieceRateService.uploadFactoryPieceRateImage(id, file);
    }

    /**
     * 删除成衣厂计件单价图片（删除磁盘文件并清空数据库路径）。
     *
     * @param id 成衣厂计件单价ID
     * @return result
     */
    @DeleteMapping("/{id}/image")
    public Result<Object> deleteFactoryPieceRateImage(@PathVariable Integer id) {
        return factoryPieceRateService.deleteFactoryPieceRateImage(id);
    }

    /**
     * 读取计件单价图片文件，兜底处理静态资源映射失效场景。
     *
     * @param request 相对图片路径
     * @return 图片文件响应
     */
    @GetMapping("/imageView/**")
    public ResponseEntity<Resource> viewFactoryPieceRateImage(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String prefix = "/admin/factoryPieceRate/imageView/";
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