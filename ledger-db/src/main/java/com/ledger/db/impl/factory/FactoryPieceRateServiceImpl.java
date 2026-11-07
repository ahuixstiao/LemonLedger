package com.ledger.db.impl.factory;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.common.exception.BizException;
import com.ledger.common.result.Result;
import com.ledger.db.entity.FactoryPieceRate;
import com.ledger.db.entity.dto.FactoryPieceRateDto;
import com.ledger.db.impl.LedgerServiceImpl;
import com.ledger.db.mapper.FactoryPieceRateMapper;
import com.ledger.db.service.factory.IFactoryPieceRateService;
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
 * 成衣厂计件单价表 服务实现类
 * </p>
 *
 * @author ahui
 * @since 2025-10-16
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FactoryPieceRateServiceImpl extends LedgerServiceImpl<FactoryPieceRateMapper, FactoryPieceRate> implements IFactoryPieceRateService {

    private static final long MAX_IMAGE_SIZE = 100L * 1024 * 1024;

    private final FactoryPieceRateMapper factoryPieceRateMapper;

    @Value("${ledger.upload.image-base-dir:../lemonLedgerImages}")
    private String imageBaseDir;

    /**
     * 查询成衣厂计件单价列表 如果有条件则按条件查询
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
    public Result<Object> queryFactoryPieceRateListByCondition(
            Integer factoryId, String styleNumber, Integer categoryId,
            Integer currentPage, Integer pageSize, Integer flag) {

        // 构建分页对象
        Page<FactoryPieceRateDto> page = new Page<>(currentPage, pageSize);

        page = factoryPieceRateMapper.selectFactoryPieceRateListByCondition(page, factoryId, styleNumber, categoryId, flag);

        return Result.ok(page);
    }

    /**
     * 按工厂ID查询对应工厂的计件单价单款式编号列表
     *
     * @param factoryId 工厂ID
     * @param flag      状态
     * @return result
     */
    @Override
    public Result<Object> queryStyleNumberListByFactoryId(Integer factoryId, Integer flag) {
        return Result.ok(
               factoryPieceRateMapper.selectStyleNumberListByFactoryId(factoryId, flag)
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
        return Result.ok(factoryPieceRateMapper.selectSampleListByStyleNumber(styleNumber, flag));
    }

    /**
     * 保存成衣厂计件单价信息
     *
     * @param factoryPieceRate 成衣厂计件单价实体
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> saveFactoryPieceRateInfo(FactoryPieceRate factoryPieceRate) {

        // 根据成衣厂ID、款式编号、工作类型、计件单价来查询是否存在完全重复的项
        boolean exists = lambdaQuery()
                .eq(FactoryPieceRate::getFactoryId, factoryPieceRate.getFactoryId())
                .eq(StrUtil.isNotEmpty(factoryPieceRate.getStyleNumber()), FactoryPieceRate::getStyleNumber, factoryPieceRate.getStyleNumber())
                .eq(ObjectUtil.isNotEmpty(factoryPieceRate.getCategoryId()), FactoryPieceRate::getCategoryId, factoryPieceRate.getCategoryId())
                .exists();

        // 存在返回提示
        if (exists) {
            throw new BizException("计件单价信息已存在：同一成衣厂 + 同一款式编号 + 同一工作类型只能有一条计件单价");
        }

        // 不存在则保存成衣厂计件单价信息
        if (!save(factoryPieceRate)) {
            throw new BizException("新增计件单价失败，请重试");
        }

        return Result.ok();
    }

    /**
     * 更新成衣厂计件单价信息
     *
     * @param factoryPieceRate 成衣厂计件单价实体
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> updateFactoryPieceRateInfo(FactoryPieceRate factoryPieceRate) {

        if (!updateById(factoryPieceRate)) {
            throw new BizException("修改计件单价失败：该计件单价不存在，或已被其他人修改，请刷新后重试");
        }

        return Result.ok();
    }

    /**
     * 上传成衣厂计件单价图片到本地外部目录。
     *
     * @param id   成衣厂计件单价ID
     * @param file 图片文件
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> uploadFactoryPieceRateImage(Integer id, MultipartFile file) {
        if (ObjectUtil.isEmpty(id)) {
            throw new BizException("计件单价单ID不能为空");
        }

        if (ObjectUtil.isEmpty(file) || file.isEmpty()) {
            throw new BizException("请先选择图片");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BizException("图片大小不能超过100MB");
        }

        FactoryPieceRate price = getById(id);
        if (ObjectUtil.isEmpty(price)) {
            throw new BizException("计件单价单不存在");
        }

        String contentType = file.getContentType();
        if (StrUtil.isBlank(contentType) || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BizException("仅支持上传图片文件");
        }

        String originalFilename = file.getOriginalFilename();
        if (StrUtil.isBlank(originalFilename)) {
            throw new BizException("无法识别图片格式");
        }

        String extension = resolveImageExtension(originalFilename);
        if (StrUtil.isBlank(extension)) {
            throw new BizException("无法识别图片格式");
        }

        Path priceDir = Paths.get(imageBaseDir, "factoryPieceRate", String.valueOf(id));
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
        Path targetPath = priceDir.resolve(fileName);

        try {
            Files.createDirectories(priceDir);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            // 带上原始异常，日志里才有堆栈（磁盘满、权限不足、路径不存在都能一眼看出来）
            throw new BizException("图片保存到磁盘失败，请稍后重试", exception);
        }

        String relativePath = Paths.get("factoryPieceRate", String.valueOf(id), fileName)
                .toString()
                .replace("\\", "/");
        price.setImagePath("/lemonLedgerImages/" + relativePath);

        if (!updateById(price)) {
            throw new BizException("图片已写入磁盘，但保存图片路径失败，请重试");
        }

        return Result.ok(price.getImagePath());
    }

    /**
     * 删除成衣厂计件单价图片（删除磁盘文件并清空数据库路径）。
     *
     * @param id 成衣厂计件单价ID
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> deleteFactoryPieceRateImage(Integer id) {
        if (ObjectUtil.isEmpty(id)) {
            throw new BizException("计件单价单ID不能为空");
        }

        FactoryPieceRate price = getById(id);
        if (ObjectUtil.isEmpty(price)) {
            throw new BizException("计件单价单不存在");
        }

        String imagePath = price.getImagePath();
        if (StrUtil.isBlank(imagePath)) {
            throw new BizException("当前单价单暂无图片");
        }

        Path targetPath = resolveImageFilePath(imagePath);
        if (ObjectUtil.isEmpty(targetPath)) {
            throw new BizException("图片路径不合法，出于安全考虑已拒绝删除");
        }

        try {
            Files.deleteIfExists(targetPath);
            cleanEmptyParentDirectories(targetPath.getParent());
        } catch (IOException exception) {
            throw new BizException("删除磁盘上的图片文件失败，请稍后重试", exception);
        }

        boolean clearSuccess = lambdaUpdate()
                .eq(FactoryPieceRate::getId, id)
                .set(FactoryPieceRate::getImagePath, null)
                .update();
        if (!clearSuccess) {
            throw new BizException("图片文件已删除，但清空数据库中的图片路径失败，请重试");
        }

        return Result.ok("删除图片成功", null);
    }

    /**
     * 删除成衣厂计件单价信息
     *
     * @param id 成衣厂计件单价ID
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> deleteFactoryPieceRateById(Integer id) {

        if (!removeById(id)) {
            throw new BizException("删除计件单价失败：该计件单价不存在或已被删除");
        }

        return Result.ok();
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
