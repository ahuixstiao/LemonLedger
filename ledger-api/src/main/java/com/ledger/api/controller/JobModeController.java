package com.ledger.api.controller;

import com.ledger.common.result.Result;
import com.ledger.db.service.IJobModeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: ahui
 * @Description: TODO 工作方式控制器
 * @DateTime: 2025/10/11 - 10:07
 **/
@RestController
@RequiredArgsConstructor(onConstructor_ =  @Autowired)
@RequestMapping("/mode")
public class JobModeController {

    private final IJobModeService jobModeService;

    @GetMapping("/list")
    public Result<Object> queryJobModeList() {

        return null;
    }

}
