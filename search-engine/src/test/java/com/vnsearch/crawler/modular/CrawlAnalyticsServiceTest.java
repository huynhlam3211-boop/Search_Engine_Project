package com.vnsearch.crawler.modular;

import com.vnsearch.crawler.bus.ImageFound;
import com.vnsearch.crawler.bus.PageEvent;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrawlAnalyticsServiceTest {
    private MeterRegistry registry;
    private CrawlAnalyticsService service;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        service = new CrawlAnalyticsService(registry);
    }

    private static PageEvent page(String url, String host, String language, int depth) {
        return new PageEvent(url, host, depth, "Tieu de", "Than bai", language,
                "<html><body>noi dung</body></html>", "hash", Instant.EPOCH, "job-1");
    }

    @Test
    void countsPagesAndExposesThemAsGauge() {}

    @Test
    void languageBecomesAPrometheusLabel() {}

    @Test
    void blankLanguageBecomesUnd() {}

    @Test
    void hostIsNeverUsedAsAPrometheusLabel() {}

    @Test
    void tracksDistinctHostsAndTopHosts() {}

    @Test
    void topHostsRespectsTheLimit() {}

    @Test
    void tracksMaximumDepthSeen() {}

    @Test
    void recordsPageSizeDistribution() {}

    @Test
    void recordsBodyTextLength() {}

    @Test
    void aggregatesImagesFromTheImageService() {}

    @Test
    void recordsImagesPerPageWhenThePageArrives() {}

    @Test
    void snapshotContainsTheHeadlineNumbers() {}

    @Test
    void blankHostIsNotTracked() {}

    @Test
    void handlerNameIsReadableInLogs() {}

    @Test
    void constructorRequiresARegistry() {}
    
}