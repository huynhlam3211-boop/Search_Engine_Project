package com.vnsearch.crawler.bus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;

public record PageEvent(
        String url,
        String host,
        int depth,
        String title,
        String bodyText,
        String language,
        String html,
        String contentHash,
        Instant crawledAt,
        String jobId) { 
    
    public PageEvent {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("PageEvent.url must not be empty");
        }
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("PageEvent.host must not be empty, url=" + url);
        }
        if (depth < 0) {
            throw new IllegalArgumentException("PageEvent.depth must be >= 0, got: " + depth);
        }
    }

    @JsonIgnore
    public int htmlSizeBytes() {
        return html == null ? 0 : html.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
    }

    public PageEvent withoutHtml() {
        return new PageEvent(url, host, depth, title, bodyText, language, null,
                contentHash, crawledAt, jobId);
    }

    @Override
    public String toString() {
        return "PageEvent{url='" + url + "', host='" + host + "', depth=" + depth
                + ", htmlBytes=" + htmlSizeBytes() + ", lang='" + language + "'}";
    }
}