package com.vnsearch.crawler.bus;

/**
 * @param url       URL đã chuẩn hoá, đã qua URL Filter và URL Seen Detector
 * @param host      host của URL — <b>khoá phân hoạch Kafka</b>, xem trên
 * @param depth     độ sâu BFS mà URL này sẽ mang khi vào frontier
 * @param sourceUrl trang đã sinh ra liên kết này, giữ để lần vết và để dựng đồ thị liên kết
 * @param jobId     phiên crawl mà URL này thuộc về — quyết định frontier nào
 *                  nhận nó khi có nhiều phiên chạy song song; xem
 *                  {@link PageEvent}
 */

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