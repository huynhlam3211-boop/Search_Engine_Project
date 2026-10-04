package com.vnsearch.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig{
    @Bean
    public KeyResolver userOrIpKeyResolver() {

    }
}