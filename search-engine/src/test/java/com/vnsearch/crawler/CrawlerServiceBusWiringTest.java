package com.vnsearch.crawler;

import com.vnsearch.crawler.bus.CrawlEventBus;
import com.vnsearch.crawler.bus.DiscoveredUrl;
import com.vnsearch.crawler.bus.InProcessCrawlEventBus;
import com.vnsearch.crawler.bus.OutlinksExtracted;
import com.vnsearch.crawler.bus.PageEvent;
import com.vnsearch.model.WebDocument;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrawlerServiceBusWiringTest {

    @Test
    void defaultConstructorBuildsAnInProcessBus() {
        CrawlerService crawler = new CrawlerService();

        assertTrue(crawler.isInProcessMode());
        assertTrue(crawler.getEventBus() instanceof InProcessCrawlEventBus);
        assertNotNull(crawler.getJobId(), "Chay dong lenh cung phai co danh tinh hop le");
    }

    @Test
    void modularServicesAreNotWiredBeforeTheFirstCrawl() {
        CrawlerService crawler = new CrawlerService();
        assertNull(crawler.getUrlExtractorService());
        assertNull(crawler.getImageDownloadService());
        assertNull(crawler.getAnalyticsService());
    }

    @Test
    void injectedBusMeansNoLocalServices() {
        CrawlEventBus external = CrawlEventBus.noop();
        CrawlerService crawler = new CrawlerService(external);

        assertFalse(crawler.isInProcessMode());
        assertSame(external, crawler.getEventBus());
    }

    @Test
    void nullBusFallsBackToInProcess() {
        CrawlerService crawler = new CrawlerService(null);
        assertTrue(crawler.isInProcessMode());
        assertTrue(crawler.getEventBus() instanceof InProcessCrawlEventBus);
    }

    @Test
    void jobIdCanBeSetButBlankValuesAreIgnored() {
        CrawlerService crawler = new CrawlerService();
        String generated = crawler.getJobId();

        crawler.setJobId("job-abc");
        assertEquals("job-abc", crawler.getJobId());

        crawler.setJobId("");
        crawler.setJobId(null);
        assertEquals("job-abc", crawler.getJobId(), "Gia tri rong khong duoc xoa id dang co");
        assertFalse(generated.equals(crawler.getJobId()));
    }

    @Test
    void acceptDiscoveredUrlPutsTheUrlStraightIntoTheFrontier() {
        CrawlerService crawler = new CrawlerService();
        assertEquals(0, crawler.getQueueSize());

        boolean added = crawler.acceptDiscoveredUrl(new DiscoveredUrl(
                "https://a.com/con", "a.com", 1, "https://a.com", "job-1"));

        assertTrue(added);
        assertEquals(1, crawler.getQueueSize());
    }

    @Test
    void acceptDiscoveredUrlIgnoresNull() {
        CrawlerService crawler = new CrawlerService();
        assertFalse(crawler.acceptDiscoveredUrl(null));
        assertEquals(0, crawler.getQueueSize());
    }

    @Test
    void frontierRejectsDuplicateUrls() {
        CrawlerService crawler = new CrawlerService();
        DiscoveredUrl url = new DiscoveredUrl("https://a.com/con", "a.com", 1,
                "https://a.com", "job-1");

        assertTrue(crawler.acceptDiscoveredUrl(url));
        assertFalse(crawler.acceptDiscoveredUrl(url));
        assertEquals(1, crawler.getQueueSize());
    }

    @Test
    void outlinksForAnUnknownUrlAreCountedAsOrphans() {
        CrawlerService crawler = new CrawlerService();

        crawler.acceptOutlinks(new OutlinksExtracted("https://a.com/chua-luu", "a.com",
                List.of("https://a.com/1"), "job-1"));

        assertEquals(1, crawler.getOrphanOutlinksCount());
    }

    @Test
    void acceptOutlinksIgnoresNull() {
        CrawlerService crawler = new CrawlerService();
        crawler.acceptOutlinks(null);
        assertEquals(0, crawler.getOrphanOutlinksCount());
    }

    @Test
    void contentStorageAcceptsOutlinksAfterTheDocumentIsSaved() {
        ContentStorage storage = new ContentStorage();
        WebDocument doc = new WebDocument(0, "https://a.com/bai", "Tieu de", "mo ta",
                "than bai", new ArrayList<>(), Instant.EPOCH);
        assertTrue(storage.save(doc));

        assertTrue(storage.applyOutlinks("https://a.com/bai",
                List.of("https://a.com/1", "https://a.com/2")));
        assertEquals(2, doc.getOutlinks().size());
    }

    @Test
    void applyOutlinksReturnsFalseForUnknownOrNullInput() {
        ContentStorage storage = new ContentStorage();
        assertFalse(storage.applyOutlinks("https://a.com/khong-co", List.of("x")));
        assertFalse(storage.applyOutlinks(null, List.of("x")));
        assertFalse(storage.applyOutlinks("https://a.com/bai", null));
    }

    @Test
    void applyOutlinksCopiesTheList() {
        ContentStorage storage = new ContentStorage();
        WebDocument doc = new WebDocument(0, "https://a.com/bai", "t", "m", "b",
                new ArrayList<>(), Instant.EPOCH);
        storage.save(doc);

        List<String> source = new ArrayList<>(List.of("https://a.com/1"));
        storage.applyOutlinks("https://a.com/bai", source);
        source.add("https://a.com/2");

        assertEquals(1, doc.getOutlinks().size());
    }

    @Test
    void fullLoopFromPageEventBackToTheFrontier() {
        CrawlerService crawler = new CrawlerService();
        InProcessCrawlEventBus bus = (InProcessCrawlEventBus) crawler.getEventBus();

        UrlFilter filter = new UrlFilter(java.util.Set.of("a.com"), 5);
        UrlSeenFilter seen = UrlSeenFilter.forMaxPages(1000);
        var extractor = new com.vnsearch.crawler.modular.UrlExtractorService(
                new LinkExtractor(), () -> filter, () -> seen, bus);

        bus.subscribePages(extractor)
                .subscribeDiscoveredUrls(crawler::acceptDiscoveredUrl)
                .subscribeOutlinks(crawler::acceptOutlinks);

        bus.publishPage(new PageEvent(
                "https://a.com/bai", "a.com", 0, "Tieu de", "Than bai", "vi",
                "<a href='https://a.com/mot'>1</a><a href='https://a.com/hai'>2</a>",
                "hash", Instant.EPOCH, crawler.getJobId()));

        assertEquals(2, crawler.getQueueSize(),
                "Hai lien ket phai di het vong bus va quay ve frontier");
    }
}
