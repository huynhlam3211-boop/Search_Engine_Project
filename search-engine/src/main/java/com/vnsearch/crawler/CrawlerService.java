package com.vnsearch.crawler;

import com.vnsearch.crawler.frontier.UrlFrontier;
import com.vnsearch.model.WebDocument;

public class CrawlerService {
    
    public List<WebDocument> crawl(List<String> seedUrls, CrawlConfig config) {
        long start = System.currentTimeMillis();

        UrlStorage urlStorage = config.urlStoragePath() == null ? UrlStorage.disabled() : UrlStorage.file(Path.of(config.urlStoragePath()));
        urlFilter = new UrlFilter(config.allowedDomains(), config.maxDepth());
        urlSeenFilter = UrlSeenFilter.forMaxPages(config.maxPages(), urlStorage);

        try {
            long replayed = urlSeenFilter.replayFromStorage();
            if (replayed > 0) {
                log.info("Đã nạp lại {} URL từ {} — những trang này sẽ không tải lại.",
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

    private void seed(List<String> seedUrls) {
        for (String seed: seedUrls) {
            String url = UrlCanonicalizer.canonicalize(seed);
            if (!urlFilter.accept(url,0)) {
                log.warn("Seed bị URL Filter loại, bỏ qua: {}", seed);
                continue;
            }
            urlSeenFilter.markSeenIFNew(url);
            frontier.addUrl(url, 0, SEED_BACKLINK_SCORE);
        }
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
}
