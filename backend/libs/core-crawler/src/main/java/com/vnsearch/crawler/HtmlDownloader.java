package com.vnsearch.crawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicLong;

public class HtmlDownloader {

    private static final Logger log = LoggerFactory.getLogger(HtmlDownloader.class);

    public static final String USER_AGENT = "VnSearchBot";
    public static final int DEFAULT_TIMEOUT_MS = 10000;
    public static final int DEFAULT_MAX_RETRIES = 2;

    private final DnsResolver dnsResolver;
    private final int timeoutMs;
    private final int maxRetries;

    private final AtomicLong downloaded = new AtomicLong();
    private final AtomicLong failed = new AtomicLong();
    private final AtomicLong retries = new AtomicLong();

    public HtmlDownloader() {
        this(new DnsResolver(), DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES);
    }

    public HtmlDownloader(DnsResolver dnsResolver) {
        this(dnsResolver, DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES);
    }

    public HtmlDownloader(DnsResolver dnsResolver, int timeoutMs, int maxRetries) {
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("timeoutMs must be > 0, got: " + timeoutMs);
        }
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries must be >= 0, got: " + maxRetries);
        }
        this.dnsResolver = dnsResolver;
        this.timeoutMs = timeoutMs;
        this.maxRetries = maxRetries;
    }

    /**
     * URL tro vao ha tang noi bo, bi chan truoc khi mo ket noi.
     *
     * <p>La {@link IOException} de goi ben ngoai bat chung voi loi mang, nhung
     * la lop rieng de {@code download} biet ma KHONG thu lai: da chan vi ly do
     * bao mat thi thu lai bao nhieu lan cung khong doi ket qua.
     */
    public static class BlockedTargetException extends IOException {
        public BlockedTargetException(String message) {
            super(message);
        }
    }

    /**
     * Chan SSRF ngay tai tang tai trang.
     *
     * <p>{@link SeedUrlValidator} da chan o tang controller, nhung URL vao day
     * con den tu outlink cua trang da crawl — khong qua controller. Kiem tra lai
     * o day de mot trang doc khong dieu huong crawler vao mang noi bo.
     */
    private void ensureTargetAllowed(String url) throws BlockedTargetException {
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            throw new BlockedTargetException(SeedUrlValidator.REJECTED);
        }

        String scheme = uri.getScheme();
        if (scheme == null
                || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new BlockedTargetException(SeedUrlValidator.REJECTED);
        }

        String host = uri.getHost();
        if (SeedUrlValidator.isBlockedHostname(host)) {
            throw new BlockedTargetException(SeedUrlValidator.REJECTED);
        }

        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            throw new BlockedTargetException(SeedUrlValidator.REJECTED);
        }
        for (InetAddress address : addresses) {
            if (SeedUrlValidator.isBlockedAddress(address)) {
                log.warn("Chan URL tro toi dia chi noi bo: {}", host);
                throw new BlockedTargetException(SeedUrlValidator.REJECTED);
            }
        }
    }

    public Document download(String url) throws IOException {
        // Chan TRUOC vong thu lai: URL bi chan khong duoc tinh la mot lan retry
        ensureTargetAllowed(url);

        dnsResolver.resolveHostOf(url);

        IOException lastError = null;
        for (int attempt = 0 ; attempt <= maxRetries; attempt ++) {
            if (attempt > 0) {
                retries.incrementAndGet();
            }
            try {
                Document document = Jsoup.connect(url)
                         .userAgent(USER_AGENT)
                         .timeout(timeoutMs)
                         .followRedirects(true)
                         .get();
                downloaded.incrementAndGet();
                return document;
            } catch (IOException e) {
                lastError = e;
            } catch (Exception e) {
                lastError = new IOException(e.getMessage(),e);
            }
        }
        failed.incrementAndGet();
        throw lastError;

    }

    public long getDownloadedCount() {
        return downloaded.get();
    }

    public long getFailedCount() {
        return failed.get();
    }

    public long getRetryCount() {
        return retries.get();
    }


}