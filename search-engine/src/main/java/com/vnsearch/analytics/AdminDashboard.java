package com.vnsearch.analytics;

import java.time.Instant;

public record AdminDashboard(
        Instant generatedAt,
        UsageSnapshot traffic,
        CorpusStats crawl,
        IndexStats index,
        AccountStats accounts) {
    
}