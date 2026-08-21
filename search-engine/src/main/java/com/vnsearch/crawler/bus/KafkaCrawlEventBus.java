package com.vnsearch.crawler.bus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.atomic.AtomicLong;

public class KafkaCrawlEventBus implements CrawlEventBus {
    
    private static final Logger log = LoggerFactory.getLogger(KafkaCrawlEventBus.class);

    private final KafkaTemplate<String, Object> template;
    private final String pagesTopic;
    private final String urlsTopic;
    private final String outlinksTopic;
    private final String imagesTopic;

    private final AtomicLong publishFailures = new AtomicLong();
    private final AtomicLong pagesPublished = new AtomicLong();
    private final AtomicLong urlsPublished = new AtomicLong();

    public KafkaCrawlEventBus(KafkaTemplate<String, Object> template, String pagesTopic,
                               String urlsTopic, String outlinksTopic, String imagesTopic) {
        if (template == null) {
            throw new IllegalArgumentException("KafkaCrawlEventBus requires a KafkaTemplate");
        }
        this.template = template;
        this.pagesTopic = require(pagesTopic, "pagesTopic");
        this.urlsTopic = require(urlsTopic, "urlsTopic");
        this.outlinksTopic = require(outlinksTopic, "outlinksTopic");
        this.imagesTopic = require(imagesTopic, "imagesTopic");
    }

    private static String require(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing topic name: " + name);
        }
        return value;
    }

    @Override
    public void publishPage(PageEvent event) {
        if (event == null) {
            return;
        }
        pagesPublished.incrementAndGet();
        send(pagesTopic, event.host(), event, event.url());
    }

    @Override
    public void publishDiscoveredUrl(DiscoveredUrl url) {
        if (url == null) {
            return;
        }
        urlsPublished.incrementAndGet();
        send(urlsTopic, url.host(), url, url.url());
    }

    @Override
    public void publishOutlinks(OutlinksExtracted outlinks) {
        if (outlinks == null) {
            return;
        }
        send(outlinksTopic, outlinks.host(), outlinks, outlinks.sourceUrl());
    }

    @Override
    public void publishImage(ImageFound image) {
        if (image == null) {
            return;
        }
        send(imagesTopic, image.host(), image, image.imageUrl());
    }

    private void send(String topic, String key, Object payload, String subject) {
        try {
            template.send(topic, key, payload).whenComplete((result, error) -> {
                if (error != null) {
                    publishFailures.incrementAndGet();
                    log.warn("Failed to publish to topic {} (key {}), payload {}: {}",
                            topic, key, subject, error.toString());
                }
            });
        } catch (Exception e) {
            publishFailures.incrementAndGet();
            log.warn("Failed to publish to topic {} (key {}), payload {}: {}",
                    topic, key, subject, e.toString());
        }
    }


    @Override
    public long getPublishFailureCount() {
        return publishFailures.get();
    }

    public long getPagesPublishedCount() {
        return pagesPublished.get();
    }

    public long getUrlsPublishedCount() {
        return urlsPublished.get();
    }
}