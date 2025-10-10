package com.ledger.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: ahui
 * @Description: TODO 测试
 * @DateTime: 2025/10/2 - 18:50
 **/
@RestController
@RequiredArgsConstructor(onConstructor_ =  @Autowired)
@RequestMapping("/hello")
public class HelloController {

    @GetMapping("/hi")
    public String hello() {
        return "Hello World!";
    }
}
