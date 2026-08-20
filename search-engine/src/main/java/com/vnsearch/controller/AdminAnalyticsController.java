package com.vnsearch.controller;

import com.vnsearch.analytics.AdminDashboard;
import com.vnsearch.analytics.UsageAnalyticsService;
import com.vnsearch.auth.Role;
import com.vnsearch.auth.SessionStore;
import com.vnsearch.auth.User;
import com.vnsearch.auth.UserService;
import com.vnsearch.service.SearchEngineFacade;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/admin/analytics")
@Validated
public class AdminAnalyticsController { 
    private static final int DEFAULT_TOP = 10;

    private final UsageAnalyticsService analytics;
    private final SearchEngineFacade facade;
    private final UserService users;
    private final SessionStore sessions;

    public AdminAnalyticsController(UsageAnalyticsService analytics, SearchEngineFacade facade,
                                     UserService users, SessionStore sessions) {
        this.analytics = analytics;
        this.facade = facade;
        this.users = users;
        this.sessions = sessions;
    }

    @GetMapping

    public AdminDashboard dashboard() {

    }

    private AdminDashboard.AccountStats accountStats() { 

    }

    @PostMapping("/reset")
    public ResponseEntity<Void> reset() { 
        
    }
}