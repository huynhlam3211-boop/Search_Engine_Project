package com.vnsearch.api;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String index() {
        return "Hello, World! VnSearch backend dang chay.";
    }

    @GetMapping("/api/hello")
    public Map<String, Object> hello(@RequestParam(defaultValue = "World") String name) {
        return Map.of(
                "message", "Hello, " + name + "!",
                "service", "vnsearch-backend");
    }
}
