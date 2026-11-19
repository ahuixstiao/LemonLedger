package com.ledger.db.service.backup;

import com.ledger.common.result.Result;

/**
 * 数据库备份的查看入口（备份本身由宿主机定时脚本 deploy/backup-mysql.sh 执行）。
 */
public interface IAdminBackupService {

    /**
     * 列出备份目录里的备份文件，并给出“是否太久没备份”的提示。
     */
    Result<Object> queryBackupList();
}
