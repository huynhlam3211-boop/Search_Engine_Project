package com.vnsearch.crawler;

public interface CrawlListener {
    default void onPageCrawled(CrawlEvent event){
    }

    default void onError(String url, Exception error) {
    }

    default void onDuplicateContent(String url) {
    }

    /** Trang bi Language Filter loai vi khong phai tieng Viet hay tieng Anh. */
    default void onForeignLanguage(String url, String language) {
    }

    default void onFinished(int totalPages , long elapsedMs) {
    }

    record CrawlEvent(int pageNumber, int maxPages, String url, int depth, int outlinks, int frontierSize, int domainCount) {
    }
}
