package com.vnsearch;

import com.vnsearch.config.PublicEndPoints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringApplication(scanBasePackages = {
    "com.vnsearch.config",
    "com.vnsearch.controller",
    "com.vnsearch.history"
})

@EnableMongoRepositories(scanBasePackages = "com.vnsearch.history")
public class HistoryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(HistoryServiceApplication.class, args);
    }

    @Bean
    public PublicEndPoints historyPubEndpoints() {
        return PublicEndPoints.of("/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html")
    }
}