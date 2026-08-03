package com.vnsearch.crawler;

public interface CrawlListener {
    default void onPageCrawled(CrawlEvent event){
    }

    default void onError(String url, Exception error) {
    }

    default void onDuplicateContent(String url) {
    }

    default void onFinished(int totalPages , long elapsedMs) {
    }

    record CrawlEvent(int pageNumber, int maxPages, String url, int depth, int outlinks, int frontierSize, int domainCount) {
    }
}
