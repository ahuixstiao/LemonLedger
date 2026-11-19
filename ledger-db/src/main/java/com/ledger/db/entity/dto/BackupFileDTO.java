package com.ledger.db.entity.dto;

import lombok.Data;

/**
 * 一个数据库备份文件的展示信息。
 */
@Data
public class BackupFileDTO {

    /** 文件名，例如 ledger-20261119-111109.sql.gz */
    private String fileName;

    /** 字节数 */
    private Long sizeBytes;

    /** 人类可读的大小，例如 424 KB */
    private String sizeText;

    /** 生成时间，yyyy-MM-dd HH:mm:ss */
    private String createdAt;

    /** 距今多少小时（向上取整），用来提示“多久没备份了” */
    private Long ageHours;
}
