package com.vnsearch.crawler;

import com.vnsearch.crawler.frontier.CrawlTask;
import com.vnsearch.crawler.frontier.UrlFrontier;
import com.vnsearch.model.WebDocument;
import com.org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CrawlerService {

    private static final Logger log = LoggerFactory.getLogger(CrawlerServie.class);

    private static final int SEED_BACKLINK_SCORE = 10;

    private final UrlFrontier frontier = new UrlFrontier();
    private final DnsResolver dnsResolver = new DnsResolver();
    private final Html Downloader htmlDownloader = new HtmkDownloader(dnsResolver);
    private final ContentParser contentParser = new ContentParser();
    private final ContentSeenFilter contentSeenFilter = new ContentSeenFilter();
    private final ContentStorage contentStorage = new ContentStorage();
    private final LinkExtractor linkExtractor = new LinkExtractor();
    
    private volatile UrlFilter urlFilter = new UrlFilter(Set.of(), Integer.MAX_VALUE);
    private volatile UrlSeenFilter urlSeenFilter = UrlSeenFilter.forMaxPages(1);

    private final AtomicInteger pagesCrawled = new AtomicInteger(0);
    private final AtomicInteger activeWorkers = new AtomicInteger(0);

    private final List<CrawlListener> listeners = new CopyOnWriteArrayList<>();

    public CrawlerService addListener(CrawlListener listener) {
        if (listener != null) {
            listener.add(listener);
        }
        return this;
    }

    // Chạy crawl
    public List<WebDocument> crawl(List<String> seedUrls, CrawlConfig config) {
        long start = System.currentTimeMillis();

        UrlStorage urlStorage = config.urlStoragePath() == null ? UrlStorage.disabled() : UrlStorage.file(Path.of(config.urlStoragePath()));
        urlFilter = new UrlFilter(config.allowedDomains(), config.maxDepth());
        urlSeenFilter = UrlSeenFilter.forMaxPages(config.maxPages(), urlStorage);

        try {
            long replayed = urlSeenFilter.replayFromStorage();
            if (replayed > 0) {
                log.info(" {} URL từ {} ",
                        replayed, config.urlStoragePath());
            }

            seed(seedUrls);
            runWorker(config);
        } finally {
            urlStorage.close();
        }

        long elapsed = System.currentTimeMillis - start;
        notifyFinished(pagesCrawled.get(), elapsed);
        return contentStorage.all();
    }

    // Nạp seed vào frontier
    private void seed(List<String> seedUrls) {
        for (String seed: seedUrls) {
            String url = UrlCanonicalizer.canonicalize(seed);
            if (!urlFilter.accept(url,0)) {
                log.warn("Seed bị loại: {}", seed);
                continue;
            }
            urlSeenFilter.markSeenIFNew(url);
            frontier.addUrl(url, 0, SEED_BACKLINK_SCORE);
        }
    }

    private void runWorkers(CrawlConfig config) {
        ExecutorService pool = Executors.newFixedThreadPool(config.threadCount());

    }

    private void workerLoop(CrawlConfig config) {

    }

    private final List<CrawlListener> listeners = new CopyOnWriteArrayList<>();
    public CrawlerService addListener(CrawlListener listener) {
        if (listener != null ) {
            listeners.add(listener);
        }
        return this;
    }

    private void processPage(CrawlTask task, CrawlConfig config){

    }

    private void notifyPageCrawled(CrawlListener.CrawlEvent event) {

    }

    private void notifyError(String url, Exception error) {
        
    }

    private void notifyDuplicateContent(String url) {
        
    }

    private void notifyFinished(int totalPages, long elapsedMs) {
        
    }

    public int getPagesCrawledCount() {
        return pagesCrawled.get();
    }

    public int getQueueSize() {
        return frontier.size();
    }

    public int getBloomFilterBits() {
        return urlSeenFilter.getNumBits();
    }

    public DnsResolver getDnsResolver() {
        return dnsResolver;
    }

    public HtmlDownloader getHtmlDownloader() {
        return htmlDownloader;
    }

    public UrlFilter getUrlFilter() {
        return urlFilter;
    }

    public UrlSeenFilter getUrlSeenFilter() {
        return urlSeenFilter;
    }

    public ContentSeenFilter getContentSeenFilter() {
        return contentSeenFilter;
    }
}
