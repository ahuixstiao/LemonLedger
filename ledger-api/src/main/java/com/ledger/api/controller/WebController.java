package com.ledger.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Author: ahui
 * @Description: TODO 处理页面跳转
 * @DateTime: 2025/10/20 - 13:14
 **/
@Controller
public class WebController {

    @GetMapping(value = {"/{path:^(?!api|assets|static|index\\.html|favicon\\.ico).*$}/**"})
    public String forward() {
        return "forward:/index.html";
    }
}
