package com.vnsearch.crawler;

import com.vnsearch.crawler.bus.CrawlEventBus;
import com.vnsearch.crawler.bus.DiscoveredUrl;
import com.vnsearch.crawler.bus.InProcessCrawlEventBus;
import com.vnsearch.crawler.bus.OutlinksExtracted;
import com.vnsearch.crawler.bus.PageEvent;
import com.vnsearch.model.WebDocument;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrawlerServiceBusWiringTest { 
    
}