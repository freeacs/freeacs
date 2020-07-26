package com.github.dptahtmq1.webv2.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api")
public class TestController {

    @GetMapping(path = "/test")
    public String test() {
        return "Spring boot with Angular 9 - It Works!";
    }

}
