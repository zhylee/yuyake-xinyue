package cn.yuyake.center.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class TestController {

    @RequestMapping("https")
    public Object getHttps() {
        // 简单返回一个字符串
        return "Hello, Https";
    }
}
