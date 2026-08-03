package com.vnsearch.crawler;

public class CrawlListener {
    default void onPageCrawled(CrawlEvent event){
    }

    default void orError(String url, Exception error) {
    }

    default void onDuplicateContent(String url) {
    }

    default void onFinished(int totalPages , long elapsedMs) {
    }

    record CrawEvent(int pageNumber, int maxPages, String url, int depth, int outlinks, int frontierSize, int domainCount) {
    }
}
