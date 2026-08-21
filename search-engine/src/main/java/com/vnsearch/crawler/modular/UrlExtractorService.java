package com.vnsearch.crawler.modular;

import com.vnsearch.crawler.LinkExtractor;
import com.vnsearch.crawler.UrlFilter;
import com.vnsearch.crawler.UrlSeenFilter;
import com.vnsearch.crawler.bus.CrawlEventBus;
import com.vnsearch.crawler.bus.DiscoveredUrl;
import com.vnsearch.crawler.bus.OutlinksExtracted;
import com.vnsearch.crawler.bus.PageEvent;
import com.vnsearch.crawler.bus.PageEventHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class UrlExtractorService implements PageEventHandler {

    private static final Logger log = LoggerFactory.getLogger(UrlExtractorService.class);

    private final LinkExtractor linkExtractor;

    private final Supplier<UrlFilter> urlFilter;
    private final Supplier<UrlSeenFilter> urlSeenFilter;

    private final CrawlEventBus bus;

    private final AtomicLong pagesProcessed = new AtomicLong();
    private final AtomicLong linksExtracted = new AtomicLong();
    private final AtomicLong linksAccepted = new AtomicLong();
    private final AtomicLong rejectedByFilter = new AtomicLong();
    private final AtomicLong rejectedAsSeen = new AtomicLong();
    private final AtomicLong pagesWithoutHtml = new AtomicLong();

    public UrlExtractorService(LinkExtractor linkExtractor,
                                Supplier<UrlFilter> urlFilter,
                                Supplier<UrlSeenFilter> urlSeenFilter,
                                CrawlEventBus bus) {
        if (linkExtractor == null || urlFilter == null || urlSeenFilter == null || bus == null) {
            throw new IllegalArgumentException(
                    "UrlExtractorService cần đủ linkExtractor, urlFilter, urlSeenFilter và bus");
        }
        this.linkExtractor = linkExtractor;
        this.urlFilter = urlFilter;
        this.urlSeenFilter = urlSeenFilter;
        this.bus = bus;
    }

    @Override
    public void onPage(PageEvent event) {
        if (event.html() == null || event.html().isBlank()) {
            pagesWithoutHtml.incrementAndGet();
            return;
        }

        Document document = Jsoup.parse(event.html(), event.url());
        List<String> outlinks = linkExtractor.extract(event.url(), document);

        pagesProcessed.incrementAndGet();
        linksExtracted.addAndGet(outlinks.size());

        bus.publishOutlinks(new OutlinksExtracted(
                event.url(), event.host(), outlinks, event.jobId()));

        int childDepth = event.depth() + 1;
        for (String link : outlinks) {
            if (!urlFilter.get().accept(link, childDepth)) {   // URL Filter
                rejectedByFilter.incrementAndGet();
                continue;
            }
            if (!urlSeenFilter.get().markSeenIfNew(link)) {    // URL Seen? -> URL Storage
                rejectedAsSeen.incrementAndGet();
                continue;
            }
            linksAccepted.incrementAndGet();
            bus.publishDiscoveredUrl(new DiscoveredUrl(
                    link, hostOf(link), childDepth, event.url(), event.jobId()));
        }

        if (log.isTraceEnabled()) {
            log.trace("URL Extractor: {} -> {} liên kết, {} được chấp nhận",
                    event.url(), outlinks.size(), linksAccepted.get());
        }
    }

    private static String hostOf(String url) {
        try {
            String host = URI.create(url).getHost();
            return host != null && !host.isBlank() ? host : url;
        } catch (Exception e) {
            return url;
        }
    }

    @Override
    public String handlerName() {
        return "URL Extractor";
    }

    // --- Số liệu cho báo cáo và cho Prometheus ---

    public long getPagesProcessedCount() {
        return pagesProcessed.get();
    }

    public long getLinksExtractedCount() {
        return linksExtracted.get();
    }

    public long getLinksAcceptedCount() {
        return linksAccepted.get();
    }

    public long getRejectedByFilterCount() {
        return rejectedByFilter.get();
    }

    public long getRejectedAsSeenCount() {
        return rejectedAsSeen.get();
    }

    public long getPagesWithoutHtmlCount() {
        return pagesWithoutHtml.get();
    }

    public double getAverageOutlinksPerPage() {
        long pages = pagesProcessed.get();
        return pages == 0 ? 0.0 : (double) linksExtracted.get() / pages;
    }
}
