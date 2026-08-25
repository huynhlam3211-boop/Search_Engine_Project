package com.vnsearch.service;

import com.vnsearch.crawler.ConsoleCrawlListener;
import com.vnsearch.crawler.CrawlConfig;
import com.vnsearch.crawler.CrawlListener;
import com.vnsearch.crawler.CrawlerService;
import com.vnsearch.crawler.UrlFilter;
import com.vnsearch.crawler.bus.CrawlEventBus;
import com.vnsearch.crawler.bus.DiscoveredUrl;
import com.vnsearch.crawler.bus.OutlinksExtracted;
import com.vnsearch.crawler.modular.ImageStore;
import com.vnsearch.model.WebDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

import java.net.URI;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
public class CrawlJobManager {

}
