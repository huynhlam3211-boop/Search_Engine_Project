package com.vnsearch.controller;

import com.vnsearch.analytics.AdminDashboard;
import com.vnsearch.analytics.UsageAnalyticsService;
import com.vnsearch.dashboard.AdminDashboardAssembler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpHeaders;

@RestController
@RequestMapping("/api/admin/analytics")
@Validated
public class AdminAnalyticsController {

    /** Số dòng mặc định cho mỗi bảng xếp hạng. */
    private static final int DEFAULT_TOP = 10;

    private final UsageAnalyticsService analytics;
    private final AdminDashboardAssembler assembler;

    public AdminAnalyticsController(UsageAnalyticsService analytics,
                                     AdminDashboardAssembler assembler) {
        this.analytics = analytics;
        this.assembler = assembler;
    }

    @GetMapping
    public AdminDashboard dashboard(
            @RequestParam(value = "top", defaultValue = "" + DEFAULT_TOP)
            @Min(value = 1, message = "top phai >= 1")
            @Max(value = 50, message = "top toi da 50")
            int top,
            HttpServletRequest request) {
        return assembler.assemble(top, request.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> reset() {
        analytics.reset();
        return ResponseEntity.noContent().build();
    }
}
