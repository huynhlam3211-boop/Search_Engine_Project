package com.vnsearch;

import com.vnsearch.config.PublicEndpoints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Duration;
import java.util.List;

@SpringBootApplication(scanBasePackages = {
        "com.vnsearch.config",     
        "com.vnsearch.controller",  
        "com.vnsearch.dashboard",   
        "com.vnsearch.analytics"    
})
public class AnalyticsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsServiceApplication.class, args);
    }

    @Bean
    public PublicEndpoints analyticsPublicEndpoints() {
        return () -> List.of(
                PublicEndpoints.method(HttpMethod.POST.name(), "/api/events"),
                new org.springframework.security.web.util.matcher
                        .AntPathRequestMatcher("/v3/api-docs/**"),
                new org.springframework.security.web.util.matcher
                        .AntPathRequestMatcher("/swagger-ui/**"),
                new org.springframework.security.web.util.matcher
                        .AntPathRequestMatcher("/swagger-ui.html"));
    }

    @Bean
    public RestClientCustomizer shortTimeouts() {
        return builder -> {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(Duration.ofSeconds(2));
            factory.setReadTimeout(Duration.ofSeconds(3));
            builder.requestFactory(factory);
        };
    }
}
