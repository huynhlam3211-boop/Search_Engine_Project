package com.vnsearch.crawler.modular;

import com.vnsearch.crawler.bus.ImageFound;
import com.vnsearch.crawler.bus.PageEvent;
import com.vnsearch.crawler.bus.PageEventHandler;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class CrawlAnalyticsService implements PageEventHandler { 
    public static final int MAX_TRACKED_HOSTS = 10_000;
    private final MeterRegistry registry;

    private final Map<String, Counter> pagesByLanguage = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> pagesByHost = new ConcurrentHashMap<>();

    private final DistributionSummary pageSizeBytes;
    private final DistributionSummary bodyTextLength;
    private final DistributionSummary imagesPerPage;

    private final AtomicLong pagesTotal = new AtomicLong();
    private final AtomicLong maxDepthSeen = new AtomicLong();
    private final AtomicLong hostsDropped = new AtomicLong();

    private final Map<String, LongAdder> imagesOfPage = new ConcurrentHashMap<>();

    
}