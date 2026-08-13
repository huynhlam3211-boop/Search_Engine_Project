package com.vnsearch.crawler.frontier;

public record CrawlTask(String url, String host, int depth) {

    public CrawlTask {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("url không được rỗng");
        }
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("host không được rỗng");
        }
        if (depth < 0) {
            throw new IllegalArgumentException("depth phải >= 0, nhận được: " + depth);
        }
    }

    @Override
    public String toString() {
        return "CrawlTask{" + url + ", depth=" + depth + "}";
    }
}