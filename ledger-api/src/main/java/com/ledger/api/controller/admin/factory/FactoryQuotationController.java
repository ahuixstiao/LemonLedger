package com.ledger.api.controller.admin.factory;

import com.ledger.db.service.factory.IFactoryQuotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: ahui
 * @Description: TODO 成衣厂报价控制器
 * @DateTime: 2025/10/16 - 11:31
 **/
@RestController
@RequiredArgsConstructor(onConstructor_ =  @Autowired)
@RequestMapping("/factoryQuotation")
public class FactoryQuotationController {

    private final IFactoryQuotationService factoryQuotationService;


}
