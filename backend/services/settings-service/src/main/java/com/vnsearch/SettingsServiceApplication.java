package com.vnsearch;

import com.vnsearch.config.PublicEndpoints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = {
        "com.vnsearch.config",
        "com.vnsearch.controller",
        "com.vnsearch.settings"
})
public class SettingsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SettingsServiceApplication.class, args);
    }

    @Bean
    public PublicEndpoints settingsPublicEndpoints() {
        return PublicEndpoints.of("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html");
    }
}
