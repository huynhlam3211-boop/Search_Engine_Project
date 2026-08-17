package com.vnsearch.crawler.bus;

public record DiscoveredUrl(String url, String host, int depth , String sourceUrl, String jobId) {
    public DiscoveredUrl {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("DiscoveredUrl.url must not be empty");
        }
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("DiscoveredUrl.host must not be empty, url=" + url);
        }
        if (depth < 0) {
            throw new IllegalArgumentException("DiscoveredUrl.depth must be >= 0, got: " + depth);
        }
    }
}