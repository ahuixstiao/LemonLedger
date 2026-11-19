package com.ledger.api.controller.admin.backup;

import com.ledger.common.result.Result;
import com.ledger.db.service.backup.IAdminBackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据库备份查看。
 */
@RestController
@RequestMapping("/admin/backup")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AdminBackupController {

    private final IAdminBackupService adminBackupService;

    @GetMapping("/list")
    public Result<Object> queryBackupList() {
        return adminBackupService.queryBackupList();
    }
}
