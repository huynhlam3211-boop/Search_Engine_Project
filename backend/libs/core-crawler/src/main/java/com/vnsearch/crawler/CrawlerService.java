package com.vnsearch.crawler;

import com.vnsearch.crawler.bus.CrawlEventBus;
import com.vnsearch.crawler.bus.DiscoveredUrl;
import com.vnsearch.crawler.bus.InProcessCrawlEventBus;
import com.vnsearch.crawler.bus.OutlinksExtracted;
import com.vnsearch.crawler.bus.PageEvent;
import com.vnsearch.crawler.frontier.CrawlTask;
import com.vnsearch.crawler.frontier.UrlFrontier;
import com.vnsearch.crawler.modular.CrawlAnalyticsService;
import com.vnsearch.crawler.modular.ImageDownloadService;
import com.vnsearch.crawler.modular.ImageStore;
import com.vnsearch.crawler.modular.UrlExtractorService;
import com.vnsearch.model.WebDocument;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CrawlerService {
    private static final Logger log = LoggerFactory.getLogger(CrawlerService.class);

    private static final int IDLE_CONFIRMATIONS_LOCAL = 3;
    private static final long IDLE_SLEEP_MS_LOCAL = 200L;

    private static final int IDLE_CONFIRMATIONS_BUS = 15;
    private static final long IDLE_SLEEP_MS_LOCAL = 1_000L;

    private final AtomicInteger pagesCrawled = new AtomicInteger(0);

    public CrawlerService() {
        this.bus = new InProcessCrawlEventBus();
        this.ownsBus = true;
        this.imageStore = null;
    }

    public CrawlerService(CrawlEventBus bus, ImageStore imageStore) {
        if (bus == null) {
            this.bus = new InProcessCrawlEventBus();
            this.ownsBus = true;
        } else {
            this.bus = bus;
            this.ownsBus = false;
        }
        this.imageStore = imageStore;
    }

    private void wireInProcessServices() {
        if (!ownsBus || urlExtractorService != null) {
            return; 
        }
        InProcessCrawlEventBus localBus = (InProcessCrawlEventBus) bus;

        UrlExtractorService extractor = new UrlExtractorService(
                new LinkExtractor(), () -> urlFilter, () -> urlSeenFilter, bus);
        ImageDownloadService images = new ImageDownloadService(bus);
        CrawlAnalyticsService analytics = new CrawlAnalyticsService(new SimpleMeterRegistry());

        localBus.subscribePages(extractor)
                .subscribePages(images)
                .subscribePages(analytics)
                .subscribeDiscoveredUrls(this::acceptDiscoveredUrl)
                .subscribeOutlinks(this::acceptOutlinks)
                .subscribeImages(analytics::onImage);

        if (imageStore != null) {
            localBus.subscribeImages(imageStore::add);
        }

        this.urlExtractorService = extractor;
        this.imageDownloadService = images;
        this.analyticsService = analytics;
    }

    public List<WebDocument> crawl(List<String> seedUrls, CrawlConfig config) {
        return crawl(seedUrls, config, List.of());
    }

    public List<WebDocument> crawl(List<String> seedUrls, CrawlConfig config, List<WebDocument> previousDocuments) {
        long start = System.currentTimeMillis();

        UrlStorage urlStorage = config.urlStoragePath() == null
                ? UrlStorage.disabled()
                : UrlStorage.file(Path.of(config.urlStoragePath()));
        urlFilter = new UrlFilter(config.allowedDomains(), config.maxDepth(),
                config.excludedHostPrefixes());
        urlSeenFilter = UrlSeenFilter.forMaxPages(config.maxPages(), urlStorage);

        wireInProcessServices();

        try {
            long replayed = urlSeenFilter.replayFromStorage();
            if (replayed > 0) {
                log.info("Đã nạp lại {} URL từ {} — những trang này sẽ không tải lại.",
                        replayed, config.urlStoragePath());
            }

            restore(previousDocuments);
            seed(seedUrls);
            runWorkers(config);
        } finally {
            urlStorage.close();
        }

        long elapsed = System.currentTimeMillis() - start;
        notifyFinished(pagesCrawled.get(), elapsed);
        return contentStorage.all();

    }
    private void restore(List<WebDocument> previousDocuments) {
        if (previousDocuments == null || previousDocuments.isEmpty()) {
            return;
        }
        int restored = 0;
        for (WebDocument doc : previousDocuments) {
            if (doc == null || doc.getUrl() == null || doc.getUrl().isBlank()) {
                continue;
            }
            if (!contentStorage.save(doc)) {
                continue; 
            }
            doc.setDocId(restored++);
            urlSeenFilter.markSeenIfNew(doc.getUrl());
            contentSeenFilter.seenBefore(doc.getBodyText());
        }
        restoredDocCount = restored;

        int queued = 0;
        for (WebDocument doc : previousDocuments) {
            if (doc == null || doc.getOutlinks() == null) {
                continue;
            }
            for (String outlink : doc.getOutlinks()) {
                if (enqueue(outlink, 1)) {
                    queued++;
                }
            }
        }
        log.info("Nối tiếp corpus cũ: giữ {} tài liệu, dựng lại frontier với {} URL chờ.",
                restored, queued);
    }

    private void seed(List<String> seedUrls) {
        for (String seed : seedUrls) {
            String url = UrlCanonicalizer.canonicalize(seed);
            if (!urlFilter.accept(url, 0)) {
                log.warn("Seed bị URL Filter loại, bỏ qua: {}", seed);
                continue;
            }
            urlSeenFilter.markSeenIfNew(url);
            frontier.addUrl(url, 0, SEED_BACKLINK_SCORE);
        }
    }

    private void runWorkers(CrawlConfig config) {
        ExecutorService pool = Executors.newFixedThreadPool(config.threadCount());
        CountDownLatch latch = new CountDownLatch(config.threadCount());
        for (int i=0; i< config.threadCount(); i++) {
            pool.submit(() -> {
                try {
                    workerLoop(config);

                } catch (Exception e) {
                    log.error("Worker dừng bất thường", e);
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            if (!latch.await(config.maxDurationMinutes(), TimeUnit.MINUTES)) {
                log.warn("Hết trần thời gian {} phút, dừng crawl với {} trang.",
                        config.maxDurationMinutes(), pagesCrawled.get());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        pool.shutdownNow();
    }

    private void workerLoop(CrawlConfig config) {
        final int idleConfirmations = ownsBus ? IDLE_CONFIRMATIONS_LOCAL : IDLE_CONFIRMATIONS_BUS;
        final long idleSleepMs = ownsBus ? IDLE_SLEEP_MS_LOCAL : IDLE_SLEEP_MS_BUS;
        int idleChecks = 0;

        while (pagesCrawled.get() < config.maxPages()) {
            CrawlTask task = frontier.nextUrl();
            if (task == null) {
                if (activeWorkers.get() == 0 && ++idleChecks >= idleConfimations) {
                    break; //end
                }
                try {
                    Thread.sleep(idleSleepMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                continue;
            }
            idleChecks = 0;
            if (!urlFilter.isAllowedByRobots(task.url())) {
                continue;
            }

            activeWorkers.incrementAndGet();
            try {
                processPage(task, config);
            } finally {
                activeWorker.decrementAndGet();
            }
        }
    }


    private void processPage(CrawlTask task, CrawlConfig) {
        Document html;
        try {

        } catch (IOException e) {
            notifyError(task.url(), e);
            return;
        }

        WebDocument doc = contentParser.parse(task.url(), html);
        if (!languageFilter.accept(doc)) {
            notifyForeignLanguage(task.url(), doc.getLanguage());
            return;
        }

        if (contentSeenFilter.seenBefore(doc.getBodyText())) {
            notifyDeplicateContent(task.url());
            return;
        }

        if (!contentStorage.save(doc)) {
            return;
        }

        int count = pagesCrawled.incrementAndGet();
        doc.setDocId(restoredDocCount + count -1);

        bus.publishPage(new PageEvent());
        notifyPageCrawled(new CrawlListener.CrawlEvent());
    }
}