package com.vnsearch.config;

import com.vnsearch.crawler.bus.DiscoveredUrl;
import com.vnsearch.crawler.bus.ImageFound;
import com.vnsearch.crawler.bus.OutlinksExtracted;
import com.vnsearch.crawler.bus.PageEvent;
import com.vnsearch.crawler.modular.CrawlAnalyticsService;
import com.vnsearch.crawler.modular.ImageDownloadService;
import com.vnsearch.crawler.modular.UrlExtractorService;
import com.vnsearch.service.CrawlJobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.crawler.bus", havingValue = "kafka")
public class CrawlKafkaListeners { 
    private static final Logger log = LoggerFactory.getLogger(CrawlKafkaListeners.class);

    private final UrlExtractorService urlExtractor;
    private final ImageDownloadService imageDownload;
    private final CrawlAnalyticsService analytics;
    private final CrawlJobManager jobManager;

    public CrawlKafkaListeners(UrlExtractorService urlExtractor,
                               ImageDownloadService imageDownload,
                               CrawlAnalyticsService analytics,
                               CrawlJobManager jobManager) {
        this.urlExtractor = urlExtractor;
        this.imageDownload = imageDownload;
        this.analytics = analytics;
        this.jobManager = jobManager;
        log.info("Cac listener Kafka da san sang: 3 Modular Service + 1 bo nap frontier");

        // --- vnsearch.pages: ba consumer group đọc cùng một luồng -----------

        @KafkaListener() {

        }

        @KafkaListener() {
            
        }

        @KafkaListener() {
            
        }

        @KafkaListener() {
            
        }

        @KafkaListener() {
            
        }

        @KafkaListener() {
            
        }
    }
}