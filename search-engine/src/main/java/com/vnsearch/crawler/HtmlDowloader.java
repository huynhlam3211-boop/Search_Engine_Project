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

public class HtmlDowloader {

    public static final String USER_AGENT = "VnSearchBot";
    public static final int DEFAULT_TIMEOUT_MS = 10000;
    public static final int DEFAULT_MAX_RETRIES = 2;

    private final DnsResolver dnsResolver;
    private final int timeoutMs;
    private final int maxRetries;

    private final AtomicLong dowloanded = new AtomicLong();
    private final AtomicLong failed = new AtomicLong();
    private final AtomicLong retries = new AtomicLong();

    public HtmlDowloader() {
        this(new DnsResolver(), DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES);
    }

    public HtmlDowloader(DnsResolver dnsResolver) {
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

    public Document download(String url) throws IOException {

        dnsResolver.resolveHostOf(url);

        IOException lastError = null;
        for (int attempt = 0 ; attempt <= maxRetries; attempt ++) {
            if (attemp > 0) {
                retries.incrementAndGet();
            }
            try {
                Document document = Jsoup.connect(url)
                         .userAgent(USER_AGENT)
                         .timeout(timeoutMs)
                         .followRedirects(true)
                         .get();
                dowload.incrementAndGet();
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

    public long getFailCount(){
        return failed.get();
    }

    public long getRetryCount() {
        return retries.get();
    }


}