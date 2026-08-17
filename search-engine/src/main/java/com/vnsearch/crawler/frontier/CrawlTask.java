package com.vnsearch.crawler.frontier;

public record CrawlTask(String url, String host, int depth) {

    public CrawlTask {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("url must not be empty");
        }
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("host must not be empty");
        }
        if (depth < 0) {
            throw new IllegalArgumentException("depth must be >= 0, got: " + depth);
        }
    }

    @Override
    public String toString() {
        return "CrawlTask{" + url + ", depth=" + depth + "}";
    }
}