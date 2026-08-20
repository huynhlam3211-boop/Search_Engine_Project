package com.vnsearch.controller;

import com.vnsearch.analytics.UsageAnalyticsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/api")
public class EventController { 
    private final UsageAnalyticsService analytics;

    public EventController(UsageAnalyticsService analytics) {
        this.analytics = analytics;
    }

    public record EventRequest(
            @NotBlank(message = "type khong duoc de trong")
            @Size(max = 16, message = "type khong hop le")
            String type,

            @Size(max = 64, message = "sessionId toi da 64 ky tu")
            String sessionId,

            @Size(max = 200, message = "query toi da 200 ky tu")
            String query,

            @Size(max = 500, message = "url toi da 500 ky tu")
            String url,

            Integer position,
            Integer resultCount,
            Long tookMs) {
    }

    @PostMapping("/events")
    public ResponseEntity<Void> record(@Valid @RequestBody EventRequest request,
                                        Authentication authentication) { 

    }
}