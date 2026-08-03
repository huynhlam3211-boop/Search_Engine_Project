package com.vnsearch.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleCrawlListener implements CrawlListener {

    private static final Logger log = LoggerFactory.getLogger(ConsoleCrawlListener.class);

    private final int logEveryNPages;

    public ConsoleCrawlListener(int logEveryNPages) {
        this.logEveryNPages = logEveryNPages <= 0 ? 1 : logEveryNPages;
    }

    @Override
    public void onPageCrawled(CrawlEvent e){
        if (e.pageNumber() % logEveryNPages != 0) {
            return;
        }
        log.info("[{}/{}] depth={} outlinks={} frontier={} domains={} {}",
                e.pageNumber(), e.maxPages(), e.depth(), e.outlinks(),
                e.frontierSize(), e.domainCount(), e.url());
    }

    @Override
    public void onError(String url, Exception error){
        log.warn("Lỗi khi crawl {}: {}", url, error.getMessage());
    }

    @Override
    public void onDuplicateContent(String url){
        log.debug("Nội dung trùng, bỏ qua: {}", url);
    }

    @Override
    public void onFinished(int totalPages, long elapsedMs){
        log.info("Crawl xong: {} trang trong {} ms", totalPages, elapsedMs);
    }
}
