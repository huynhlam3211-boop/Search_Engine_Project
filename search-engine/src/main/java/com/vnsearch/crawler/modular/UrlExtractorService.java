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

    
}