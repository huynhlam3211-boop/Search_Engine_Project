package com.vnsearch.crawler.bus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;

/**
 * @param url           URL đã chuẩn hoá của trang
 * @param host          host tách sẵn — cũng chính là <b>khoá phân hoạch</b> Kafka
 * @param depth         độ sâu BFS tại thời điểm trang được tải
 * @param title         tiêu đề do {@code ContentParser} bóc
 * @param bodyText      văn bản thân bài, để Analytics đo mà không phải phân tích lại DOM
 * @param language      mã ngôn ngữ do {@code LanguageFilter} kết luận
 * @param html          HTML thô — nguồn dữ liệu của URL Extractor và Image Download
 * @param contentHash   vân tay SHA-256 mà {@code ContentSeenFilter} đã tính
 * @param crawledAt     thời điểm tải xong
 * @param jobId         phiên crawl đã sinh ra trang này — xem phần dưới
 */

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
            throw new IllegalArgumentException("PageEvent.url không được rỗng");
        }
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("PageEvent.host không được rỗng, url=" + url);
        }
        if (depth < 0) {
            throw new IllegalArgumentException("PageEvent.depth phải >= 0, nhận được: " + depth);
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