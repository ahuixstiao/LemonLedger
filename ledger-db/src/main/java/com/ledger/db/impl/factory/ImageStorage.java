package com.ledger.db.impl.factory;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.ledger.common.exception.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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
 * 样板图片的落盘与删除：把「/lemonLedgerImages/xxx」这种访问路径翻译成磁盘路径，
 * 并处理空目录清理，避免各业务重复写一份。
 *
 * @author ahui
 * @since 2026-11-27
 */
@Component
public class ImageStorage {

    private static final long MAX_IMAGE_SIZE = 100L * 1024 * 1024;

    private static final String ACCESS_PREFIX = "/lemonLedgerImages/";

    @Value("${ledger.upload.image-base-dir:../lemonLedgerImages}")
    private String imageBaseDir;

    /**
     * 保存上传的图片到「<根目录>/<子目录>/<随机名><后缀>」，返回可访问路径。
     *
     * @param file      上传的文件
     * @param subDir    子目录，例如 factoryStyle
     * @param ownerId   归属ID，作为二级目录
     * @return 形如 /lemonLedgerImages/factoryStyle/12/xxx.png 的访问路径
     */
    public String save(MultipartFile file, String subDir, Integer ownerId) {
        if (ObjectUtil.isEmpty(file) || file.isEmpty()) {
            throw new BizException("请先选择图片");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BizException("图片大小不能超过100MB");
        }
        String contentType = file.getContentType();
        if (StrUtil.isBlank(contentType) || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BizException("仅支持上传图片文件");
        }
        String originalFilename = file.getOriginalFilename();
        if (StrUtil.isBlank(originalFilename)) {
            throw new BizException("无法识别图片格式");
        }
        String extension = resolveExtension(originalFilename);
        if (StrUtil.isBlank(extension)) {
            throw new BizException("无法识别图片格式");
        }

        Path targetDir = Paths.get(imageBaseDir, subDir, String.valueOf(ownerId));
        Path targetPath = targetDir.resolve(UUID.randomUUID().toString().replace("-", "") + extension);
        try {
            Files.createDirectories(targetDir);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new BizException("图片保存到磁盘失败，请稍后重试", exception);
        }

        return ACCESS_PREFIX + Paths.get(subDir, String.valueOf(ownerId), targetPath.getFileName().toString())
                .toString()
                .replace("\\", "/");
    }

    /**
     * 删除访问路径对应的磁盘文件并向上清理空目录。
     *
     * @param accessPath 数据库里的访问路径
     */
    public void delete(String accessPath) {
        Path targetPath = resolveFilePath(accessPath);
        if (ObjectUtil.isEmpty(targetPath)) {
            throw new BizException("图片路径不合法，出于安全考虑已拒绝删除");
        }
        try {
            Files.deleteIfExists(targetPath);
            cleanEmptyParents(targetPath.getParent());
        } catch (IOException exception) {
            throw new BizException("删除磁盘上的图片文件失败，请稍后重试", exception);
        }
    }

    /**
     * 从原始文件名里取小写后缀。
     */
    private String resolveExtension(String originalFilename) {
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(dotIndex).toLowerCase(Locale.ROOT);
    }

    /**
     * 访问路径 → 磁盘绝对路径，越界返回 null。
     */
    private Path resolveFilePath(String accessPath) {
        if (StrUtil.isBlank(accessPath) || !accessPath.startsWith(ACCESS_PREFIX)) {
            return null;
        }
        String relativePath = accessPath.substring(ACCESS_PREFIX.length());
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
     * 从起始目录向上删空目录，最多到图片根目录。
     */
    private void cleanEmptyParents(Path startPath) {
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
                if (!Files.isDirectory(currentPath)) {
                    break;
                }
                boolean empty;
                try (Stream<Path> pathStream = Files.list(currentPath)) {
                    empty = pathStream.findAny().isEmpty();
                }
                if (!empty) {
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
