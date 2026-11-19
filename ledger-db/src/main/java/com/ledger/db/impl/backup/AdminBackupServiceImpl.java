package com.ledger.db.impl.backup;

import com.ledger.common.result.Result;
import com.ledger.db.entity.dto.BackupFileDTO;
import com.ledger.db.service.backup.IAdminBackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 读取备份目录，把文件列表和“最近一次备份”状态返回给后台页面。
 * <p>
 * 真正的备份动作在宿主机上由 deploy/backup-mysql.sh（+ Windows 计划任务）完成，
 * 容器里只挂了这个目录的只读视图，不做任何写操作。
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AdminBackupServiceImpl implements IAdminBackupService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 超过这个小时数没新备份就提示“该检查计划任务了” */
    private static final long STALE_HOURS = 36L;

    @Value("${ledger.backup.dir:/backups}")
    private String backupDir;

    @Override
    public Result<Object> queryBackupList() {
        File dir = new File(backupDir);

        List<BackupFileDTO> files = new ArrayList<>();
        if (dir.isDirectory()) {
            File[] list = dir.listFiles((d, name) -> name.startsWith("ledger-") && name.endsWith(".sql.gz"));
            if (list != null) {
                Arrays.sort(list, Comparator.comparingLong(File::lastModified).reversed());
                for (File file : list) {
                    files.add(toDTO(file));
                }
            }
        }

        BackupFileDTO latest = files.isEmpty() ? null : files.get(0);
        long totalBytes = 0L;
        for (BackupFileDTO file : files) {
            totalBytes += file.getSizeBytes() == null ? 0L : file.getSizeBytes();
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("dir", backupDir);
        data.put("files", files);
        data.put("fileCount", files.size());
        data.put("totalBytes", totalBytes);
        data.put("totalSizeText", readableSize(totalBytes));
        data.put("latest", latest);
        data.put("stale", latest == null || latest.getAgeHours() == null || latest.getAgeHours() > STALE_HOURS);
        data.put("staleHours", STALE_HOURS);
        return Result.ok(data);
    }

    private BackupFileDTO toDTO(File file) {
        BackupFileDTO dto = new BackupFileDTO();
        dto.setFileName(file.getName());
        dto.setSizeBytes(file.length());
        dto.setSizeText(readableSize(file.length()));
        LocalDateTime created = LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault());
        dto.setCreatedAt(created.format(TIME_FORMATTER));
        long hours = Duration.between(created, LocalDateTime.now()).toHours();
        dto.setAgeHours(hours);
        return dto;
    }

    private String readableSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        }
        return String.format("%.1f MB", bytes / 1024.0 / 1024.0);
    }
}
