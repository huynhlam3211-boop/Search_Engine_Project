package com.vnsearch.crawler.modular;

import com.vnsearch.crawler.bus.ImageFound;
import com.vnsearch.crawler.bus.PageEvent;
import com.vnsearch.crawler.bus.PageEventHandler;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class CrawlAnalyticsService implements PageEventHandler {

    /** Trần số host theo dõi riêng — xem Javadoc lớp. */
    public static final int MAX_TRACKED_HOSTS = 10_000;

    private final MeterRegistry registry;
    private final Map<String, Counter> pagesByLanguage = new ConcurrentHashMap<>();

    /** host -> số trang. Có trần, KHÔNG đẩy sang Prometheus. */
    private final Map<String, LongAdder> pagesByHost = new ConcurrentHashMap<>();

    private final DistributionSummary pageSizeBytes;
    private final DistributionSummary bodyTextLength;
    private final DistributionSummary imagesPerPage;

    private final Counter imagesTotal;
    private final Counter imagesMissingAlt;

    private final AtomicLong pagesTotal = new AtomicLong();
    private final AtomicLong maxDepthSeen = new AtomicLong();
    private final AtomicLong hostsDropped = new AtomicLong();

    private final Map<String, LongAdder> imagesOfPage = new ConcurrentHashMap<>();

    public CrawlAnalyticsService(MeterRegistry registry) {
        if (registry == null) {
            throw new IllegalArgumentException("CrawlAnalyticsService cần một MeterRegistry");
        }
        this.registry = registry;

        this.pageSizeBytes = DistributionSummary.builder("vnsearch.crawl.page.size.bytes")
                .description("Kich thuoc HTML tho cua trang da crawl")
                .baseUnit("bytes")
                .publishPercentileHistogram()
                .register(registry);

        this.bodyTextLength = DistributionSummary.builder("vnsearch.crawl.page.text.chars")
                .description("So ky tu than bai sau khi boc")
                .register(registry);

        this.imagesPerPage = DistributionSummary.builder("vnsearch.crawl.page.images")
                .description("So anh tim thay tren moi trang")
                .register(registry);

        this.imagesTotal = Counter.builder("vnsearch.crawl.images.total")
                .description("Tong so anh tim thay")
                .register(registry);

        this.imagesMissingAlt = Counter.builder("vnsearch.crawl.images.missing.alt.total")
                .description("So anh khong co thuoc tinh alt")
                .register(registry);

        Gauge.builder("vnsearch.crawl.pages.total", pagesTotal, AtomicLong::get)
                .description("Tong so trang da qua Analytics Service")
                .register(registry);

        Gauge.builder("vnsearch.crawl.hosts.distinct", pagesByHost, Map::size)
                .description("So host phan biet da gap (chan tren "
                        + MAX_TRACKED_HOSTS + ")")
                .register(registry);

        Gauge.builder("vnsearch.crawl.depth.max", maxDepthSeen, AtomicLong::get)
                .description("Do sau BFS lon nhat da cham toi")
                .register(registry);
    }

    @Override
    public void onPage(PageEvent event) {
        pagesTotal.incrementAndGet();

        String language = event.language() == null || event.language().isBlank()
                ? "und" : event.language();
        pagesByLanguage.computeIfAbsent(language, lang -> Counter
                        .builder("vnsearch.crawl.pages.by.language.total")
                        .tag("language", lang)
                        .description("So trang da crawl theo ngon ngu")
                        .register(registry))
                .increment();

        pageSizeBytes.record(event.htmlSizeBytes());
        if (event.bodyText() != null) {
            bodyTextLength.record(event.bodyText().length());
        }

        trackHost(event.host());
        maxDepthSeen.updateAndGet(current -> Math.max(current, event.depth()));

        LongAdder counted = imagesOfPage.remove(event.url());
        if (counted != null) {
            imagesPerPage.record(counted.sum());
        }
    }

    public void onImage(ImageFound image) {
        imagesTotal.increment();
        if (image.missingAlt()) {
            imagesMissingAlt.increment();
        }
        imagesOfPage.computeIfAbsent(image.pageUrl(), url -> new LongAdder()).increment();
    }

    private void trackHost(String host) {
        if (host == null || host.isBlank()) {
            return;
        }
        LongAdder counter = pagesByHost.get(host);
        if (counter != null) {
            counter.increment();
            return;
        }
        if (pagesByHost.size() >= MAX_TRACKED_HOSTS) {
            hostsDropped.incrementAndGet();
            return;
        }
        pagesByHost.computeIfAbsent(host, h -> new LongAdder()).increment();
    }

    @Override
    public String handlerName() {
        return "Analytics Service";
    }


    public long getPagesTotal() {
        return pagesTotal.get();
    }

    public int getDistinctHostCount() {
        return pagesByHost.size();
    }

    /** Số host bị bỏ qua vì bảng đã chạm trần. */
    public long getHostsDroppedCount() {
        return hostsDropped.get();
    }

    public long getMaxDepthSeen() {
        return maxDepthSeen.get();
    }

    public Map<String, Long> topHosts(int n) {
        return pagesByHost.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, LongAdder>>comparingLong(
                        e -> e.getValue().sum()).reversed())
                .limit(Math.max(0, n))
                .collect(LinkedHashMap::new,
                        (map, e) -> map.put(e.getKey(), e.getValue().sum()),
                        LinkedHashMap::putAll);
    }

    public Map<String, Object> snapshot() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("pagesTotal", pagesTotal.get());
        stats.put("distinctHosts", pagesByHost.size());
        stats.put("hostsDropped", hostsDropped.get());
        stats.put("maxDepth", maxDepthSeen.get());
        stats.put("topHosts", topHosts(10));
        return stats;
    }

    public List<String> languagesSeen() {
        return pagesByLanguage.keySet().stream().sorted().toList();
    }
}
