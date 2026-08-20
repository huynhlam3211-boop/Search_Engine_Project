package com.vnsearch.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitFilter extends OncePerRequestFilter { 
    private static final int MAX_TRACKED_CLIENTS = 100_000;

    private final int capacity;
    private final boolean enabled;
    private final boolean trustProxy;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(int requestsPerMinute, boolean enabled) {
        this(requestsPerMinute, enabled, false);
    }

    public RateLimitFilter(int requestPerMinute, boolean enabled, boolean trustProxy) {

    }

    static final class Bucket {
        private final double 
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response) {
        
    }

    private String clientIp(HttpServletRequest request) {

    }
}