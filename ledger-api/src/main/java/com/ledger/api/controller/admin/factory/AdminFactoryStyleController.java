package com.ledger.api.controller.admin.factory;

import com.ledger.common.result.Result;
import com.ledger.db.entity.FactoryStyle;
import com.ledger.db.service.factory.IFactoryStyleService;
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
 * <p>
 * 成衣厂款式控制器：款式编号与样板图片的唯一维护入口
 * </p>
 *
 * @author ahui
 * @since 2026-11-27
 */
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/admin/factoryStyle")
public class AdminFactoryStyleController {

    private final IFactoryStyleService factoryStyleService;

    @Value("${ledger.upload.image-base-dir:../lemonLedgerImages}")
    private String imageBaseDir;

    /**
     * 分页查询成衣厂款式
     *
     * @param factoryId   成衣厂ID
     * @param styleNumber 款式编号（模糊）
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @param flag        删除状态 0否 1是
     * @return result
     */
    @GetMapping
    public Result<Object> queryFactoryStyleListByCondition(
            @RequestParam(required = false) String factoryId,
            @RequestParam(required = false) String styleNumber,
            @RequestParam(required = false, defaultValue = "1") String currentPage,
            @RequestParam(required = false, defaultValue = "10") String pageSize,
            @RequestParam(required = false, defaultValue = "0") String flag) {

        return factoryStyleService.queryFactoryStyleListByCondition(
                parseIntegerParam(factoryId),
                styleNumber,
                parseIntegerParam(currentPage, 1),
                parseIntegerParam(pageSize, 10),
                parseIntegerParam(flag, 0));
    }

    /**
     * 按成衣厂查款式下拉列表（计件单价弹窗用）
     *
     * @param factoryId 成衣厂ID
     * @param flag      删除状态 0否 1是
     * @return result
     */
    @GetMapping("/byFactory")
    public Result<Object> queryStyleListByFactoryId(
            @RequestParam(required = false) String factoryId,
            @RequestParam(required = false, defaultValue = "0") String flag) {
        return factoryStyleService.queryStyleListByFactoryId(
                parseIntegerParam(factoryId),
                parseIntegerParam(flag, 0));
    }

    /**
     * 新增成衣厂款式
     *
     * @param factoryStyle 款式实体
     * @return result
     */
    @PostMapping
    public Result<Object> saveFactoryStyle(@RequestBody FactoryStyle factoryStyle) {
        return factoryStyleService.saveFactoryStyle(factoryStyle);
    }

    /**
     * 修改成衣厂款式
     *
     * @param id           款式ID
     * @param factoryStyle 款式实体
     * @return result
     */
    @PutMapping("/{id}")
    public Result<Object> updateFactoryStyle(@PathVariable Integer id,
                                             @RequestBody FactoryStyle factoryStyle) {
        factoryStyle.setId(id);
        return factoryStyleService.updateFactoryStyle(factoryStyle);
    }

    /**
     * 删除成衣厂款式（有计件单价引用时会被拒绝）
     *
     * @param id 款式ID
     * @return result
     */
    @DeleteMapping("/{id}")
    public Result<Object> deleteFactoryStyle(@PathVariable Integer id) {
        return factoryStyleService.deleteFactoryStyle(id);
    }

    /**
     * 上传样板图片
     *
     * @param id   款式ID
     * @param file 图片文件
     * @return result，data 为新访问路径
     */
    @PostMapping("/{id}/image")
    public Result<Object> uploadFactoryStyleImage(@PathVariable Integer id,
                                                  @RequestParam("file") MultipartFile file) {
        return factoryStyleService.uploadFactoryStyleImage(id, file);
    }

    /**
     * 删除样板图片
     *
     * @param id 款式ID
     * @return result
     */
    @DeleteMapping("/{id}/image")
    public Result<Object> deleteFactoryStyleImage(@PathVariable Integer id) {
        return factoryStyleService.deleteFactoryStyleImage(id);
    }

    /**
     * 读取样板图片文件（静态资源映射失效时的兜底）
     *
     * @param request 请求对象
     * @return 图片响应
     */
    @GetMapping("/imageView/**")
    public ResponseEntity<Resource> viewFactoryStyleImage(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String prefix = "/admin/factoryStyle/imageView/";
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
