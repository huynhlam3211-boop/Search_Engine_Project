package com.vnsearch.controller;

import com.vnsearch.crawler.SeedUrlValidator;
import com.vnsearch.service.SearchEngineFacade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Validated
public class AdminController { 
    private static final int MAX_PAGES_LIMIT = 50_000;
    private static final int MAX_DEPTH_LIMIT = 10;
    private static final int MAX_SEEDS = 50;

    private final SearchEngineFacade facade;

    public AdminController(SearchEngineFacade facade) {
        this.facade = facade;
    }

    /**
     * @param maxDepth {@code null} nghia la dung mac dinh 3
     * @param maxPages {@code null} nghia la dung mac dinh 100
     */

    public record CrawlRequest() {

    }

    @PostMapping("/crawl")
    public Map<String, String> crawl(@Valid @RequestBody CrawlRequest request) { 

    }

    @GetMapping("/crawl/{jobId}/status")
    public ResponseEntity<Map<String, Object>> crawlStatus(@PathVariable String jobId) { 

    }

    @PostMapping("/reindex")
    public Map<String, String> reindex() throws IOException { 

    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        
    }


}