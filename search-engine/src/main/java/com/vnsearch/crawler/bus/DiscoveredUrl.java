package com.vnsearch.crawler.bus;

public record DiscoveredUrl(String url, String host, int depth , String sourceUrl, String jobId) {
    public DiscoveredUrl {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("DiscoveredUrl.url không được rỗng");
        }
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("DiscoveredUrl.host không được rỗng, url=" + url);
        }
        if (depth < 0) {
            throw new IllegalArgumentException("DiscoveredUrl.depth phải >= 0, nhận được: " + depth);
        }
    }
}