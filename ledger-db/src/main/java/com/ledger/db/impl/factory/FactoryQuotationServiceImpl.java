package com.ledger.db.impl.factory;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ledger.common.result.Result;
import com.ledger.db.entity.FactoryQuotation;
import com.ledger.db.entity.dto.FactoryQuotationDto;
import com.ledger.db.mapper.FactoryQuotationMapper;
import com.ledger.db.service.factory.IFactoryQuotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * <p>
 * 成衣厂报价表 服务实现类
 * </p>
 *
 * @author ahui
 * @since 2025-10-16
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FactoryQuotationServiceImpl extends ServiceImpl<FactoryQuotationMapper, FactoryQuotation> implements IFactoryQuotationService {

    private static final long MAX_IMAGE_SIZE = 100L * 1024 * 1024;

    private final FactoryQuotationMapper factoryQuotationMapper;

    @Value("${ledger.upload.image-base-dir:../lemonLedgerImages}")
    private String imageBaseDir;

    /**
     * 查询成衣厂报价列表 如果有条件则按条件查询
     *
     * @param factoryId   工厂ID
     * @param styleNumber 款式编号
     * @param categoryId  工作类型ID
     * @param currentPage 当前页
     * @param pageSize    页面条数
     * @param flag        状态
     * @return result
     */
    @Override
    @Transactional(readOnly = true)
    public Result<Object> queryFactoryQuotationListByCondition(
            Integer factoryId, String styleNumber, Integer categoryId,
            Integer currentPage, Integer pageSize, Integer flag) {

        // 构建分页对象
        Page<FactoryQuotationDto> page = new Page<>(currentPage, pageSize);

        page = factoryQuotationMapper.selectFactoryQuotationListByCondition(page, factoryId, styleNumber, categoryId, flag);

        return Result.ok(page);
    }

    /**
     * 按工厂ID查询对应工厂的报价单款式编号列表
     *
     * @param factoryId 工厂ID
     * @param flag      状态
     * @return result
     */
    @Override
    public Result<Object> queryStyleNumberListByFactoryId(Integer factoryId, Integer flag) {
        return Result.ok(
               factoryQuotationMapper.selectStyleNumberListByFactoryId(factoryId, flag)
        );
    }

    /**
     * 员工端按款式编号查询样板列表（仅包含款式编号和图片路径）。
     *
     * @param styleNumber 款式编号（支持模糊匹配）
     * @param flag        删除状态 0否 1是
     * @return result
     */
    @Override
    @Transactional(readOnly = true)
    public Result<Object> querySampleListByStyleNumber(String styleNumber, Integer flag) {
        return Result.ok(factoryQuotationMapper.selectSampleListByStyleNumber(styleNumber, flag));
    }

    /**
     * 保存成衣厂报价单信息
     *
     * @param factoryQuotation 成衣厂报价单实体
     * @return result
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Object> saveFactoryQuotationInfo(FactoryQuotation factoryQuotation) {

        // 根据成衣厂ID、款式编号、工作类型、报价来查询是否存在完全重复的项
        boolean exists = lambdaQuery()
                .eq(FactoryQuotation::getFactoryId, factoryQuotation.getFactoryId())
                .eq(StrUtil.isNotEmpty(factoryQuotation.getStyleNumber()), FactoryQuotation::getStyleNumber, factoryQuotation.getStyleNumber())
                .eq(ObjectUtil.isNotEmpty(factoryQuotation.getCategoryId()), FactoryQuotation::getCategoryId, factoryQuotation.getCategoryId())
                .exists();

        // 存在返回提示
        if (exists) {
            return Result.fail("报价信息已存在");
        }

        // 不存在则保存成衣厂报价信息
        if (save(factoryQuotation)) {
            return Result.ok();
        }

        return Result.fail();
    }

    /**
     * 更新成衣厂报价单信息
     *
     * @param factoryQuotation 成衣厂报价单实体
     * @return result
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Object> updateFactoryQuotationInfo(FactoryQuotation factoryQuotation) {

        if (updateById(factoryQuotation)) {
            return Result.ok();
        }

        return Result.fail();
    }

    /**
     * 上传成衣厂报价单图片到本地外部目录。
     *
     * @param id   成衣厂报价单ID
     * @param file 图片文件
     * @return result
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Object> uploadFactoryQuotationImage(Integer id, MultipartFile file) {
        if (ObjectUtil.isEmpty(id)) {
            return Result.fail("报价单ID不能为空");
        }

        if (ObjectUtil.isEmpty(file) || file.isEmpty()) {
            return Result.fail("请先选择图片");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            return Result.fail("图片大小不能超过100MB");
        }

        FactoryQuotation quotation = getById(id);
        if (ObjectUtil.isEmpty(quotation)) {
            return Result.fail("报价单不存在");
        }

        String contentType = file.getContentType();
        if (StrUtil.isBlank(contentType) || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            return Result.fail("仅支持上传图片文件");
        }

        String originalFilename = file.getOriginalFilename();
        if (StrUtil.isBlank(originalFilename)) {
            return Result.fail("无法识别图片格式");
        }

        String extension = resolveImageExtension(originalFilename);
        if (StrUtil.isBlank(extension)) {
            return Result.fail("无法识别图片格式");
        }

        Path quotationDir = Paths.get(imageBaseDir, "factoryQuotation", String.valueOf(id));
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
        Path targetPath = quotationDir.resolve(fileName);

        try {
            Files.createDirectories(quotationDir);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            return Result.fail("上传失败，请稍后重试");
        }

        String relativePath = Paths.get("factoryQuotation", String.valueOf(id), fileName)
                .toString()
                .replace("\\", "/");
        quotation.setImagePath("/lemonLedgerImages/" + relativePath);

        if (!updateById(quotation)) {
            return Result.fail("图片上传成功，但保存路径失败");
        }

        return Result.ok(quotation.getImagePath());
    }

    /**
     * 删除成衣厂报价单图片（删除磁盘文件并清空数据库路径）。
     *
     * @param id 成衣厂报价单ID
     * @return result
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Object> deleteFactoryQuotationImage(Integer id) {
        if (ObjectUtil.isEmpty(id)) {
            return Result.fail("报价单ID不能为空");
        }

        FactoryQuotation quotation = getById(id);
        if (ObjectUtil.isEmpty(quotation)) {
            return Result.fail("报价单不存在");
        }

        String imagePath = quotation.getImagePath();
        if (StrUtil.isBlank(imagePath)) {
            return Result.fail("当前报价单暂无图片");
        }

        Path targetPath = resolveImageFilePath(imagePath);
        if (ObjectUtil.isEmpty(targetPath)) {
            return Result.fail("图片路径不合法");
        }

        try {
            Files.deleteIfExists(targetPath);
            cleanEmptyParentDirectories(targetPath.getParent());
        } catch (IOException exception) {
            return Result.fail("删除图片失败，请稍后重试");
        }

        boolean clearSuccess = lambdaUpdate()
                .eq(FactoryQuotation::getId, id)
                .set(FactoryQuotation::getImagePath, null)
                .update();
        if (!clearSuccess) {
            return Result.fail("图片删除成功，但清空路径失败");
        }

        return Result.ok("删除图片成功", null);
    }

    /**
     * 删除成衣厂报价单信息
     *
     * @param id 成衣厂报价单ID
     * @return result
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Object> deleteFactoryQuotationById(Integer id) {

        if (removeById(id)) {
            return Result.ok();
        }

        return Result.fail();
    }

    /**
     * 从文件名中提取图片后缀。
     *
     * @param originalFilename 原始文件名
     * @return 图片后缀，例如 .jpg
     */
    private String resolveImageExtension(String originalFilename) {
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(dotIndex).toLowerCase(Locale.ROOT);
    }

    /**
     * 将数据库中的访问路径转换为磁盘绝对路径。
     *
     * @param imagePath 数据库中的图片访问路径
     * @return 磁盘路径，非法时返回 null
     */
    private Path resolveImageFilePath(String imagePath) {
        String prefix = "/lemonLedgerImages/";
        if (!imagePath.startsWith(prefix)) {
            return null;
        }

        String relativePath = imagePath.substring(prefix.length());
        if (StrUtil.isBlank(relativePath)) {
            return null;
        }

        Path basePath = Paths.get(imageBaseDir).toAbsolutePath().normalize();
        Path targetPath = basePath.resolve(relativePath).normalize();

        if (!targetPath.startsWith(basePath)) {
            return null;
        }

        return targetPath;
    }

    /**
     * 清理空的父级目录，最多向上清理到图片根目录。
     *
     * @param startPath 起始目录
     */
    private void cleanEmptyParentDirectories(Path startPath) {
        if (ObjectUtil.isEmpty(startPath)) {
            return;
        }

        Path basePath = Paths.get(imageBaseDir).toAbsolutePath().normalize();
        Path currentPath = startPath;

        while (ObjectUtil.isNotEmpty(currentPath)
                && currentPath.startsWith(basePath)
                && !currentPath.equals(basePath)) {
            try {
                if (!Files.exists(currentPath)) {
                    currentPath = currentPath.getParent();
                    continue;
                }

                boolean isEmptyDirectory = false;
                if (Files.isDirectory(currentPath)) {
                    try (Stream<Path> pathStream = Files.list(currentPath)) {
                        isEmptyDirectory = pathStream.findAny().isEmpty();
                    }
                }

                if (!isEmptyDirectory) {
                    break;
                }

                Files.deleteIfExists(currentPath);
                currentPath = currentPath.getParent();
            } catch (IOException exception) {
                break;
            }
        }
    }
}
