package com.vnsearch.crawler.bus;

public interface CrawlEventBus {
    void publishPage(PageEvent event);
    void publishDiscoveredUrl(DiscoveredUrl url);
    void publishOutlinks(OutlinksExtracted outlinks);
    void publishImage(ImageFound image);
    
    long getPublishFailureCount();

    static CrawlEventBus noop() {
        return new CrawlEventBus() {
            @Override public void publishPage(PageEvent event) { }
            @Override public void publishDiscoveredUrl(DiscoveredUrl url) { }
            @Override public void publishOutlinks(OutlinksExtracted outlinks) { }
            @Override public void publishImage(ImageFound image) { }
            @Override public long getPublishFailureCount() { return 0L; }
        };
    }
}