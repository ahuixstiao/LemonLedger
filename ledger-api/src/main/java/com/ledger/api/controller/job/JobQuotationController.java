package com.ledger.api.controller.job;

import com.ledger.db.service.job.IJobQuotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: ahui
 * @Description: TODO 工作报价控制器
 * @DateTime: 2025/10/16 - 11:35
 **/
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/jobQuotation")
public class JobQuotationController {

    private final IJobQuotationService jobQuotationService;

}
