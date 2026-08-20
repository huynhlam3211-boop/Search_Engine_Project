package com.vnsearch.controller;

import com.vnsearch.model.SearchResponse;
import com.vnsearch.service.SearchEngineFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SearchController { 
    private final UsageAnalyticsService analytics;

    public EventController(UsageAnalyticsService analytics) {
        this.analytics = analytics;
    }

    public record EventRequest() {

    }

    @PostMapping("/events")
    public ResponseEntity<Void> record(@Valid @RequestBody EventRequest request,
                                        Authentication authentication) { 

    }
}