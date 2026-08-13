package com.vnsearch.crawler.bus;

public interface CrawlEventBus {
    void publishPage(PageEvent event);
    void publishDiscoveredUrl(DiscoveredUrl url);
    void publishOutlinks(OutlinksExtracted outlinks);
    void publishImage(ImageFound image);
    
    long getPublishFailureCount();

    static CrawlEventVus noop() {
        return new CrawlEventBus() {
            @Override public void publishPage(PageEvent event) { /* vứt */ }
            @Override public void publishDiscoveredUrl(DiscoveredUrl url) { /* vứt */ }
            @Override public void publishOutlinks(OutlinksExtracted outlinks) { /* vứt */ }
            @Override public void publishImage(ImageFound image) { /* vứt */ }
            @Override public long getPublishFailureCount() { return 0L; }
        }
    }
}