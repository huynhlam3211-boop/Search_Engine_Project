package com.vnsearch.crawler;

import com.vnsearch.datastructure.LRUCache;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

public class DnsResolver {
    
    public static final int DEFAULT_CACHE_SIZE = 1000;
    private final LRUCache<String, InetAddress> cache;

    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();
    private final AtomicLong failures = new AtomicLong();

    public DnsResolver() {
        this(DEFAULT_CACHE_SIZE);
    }

    public DnsResolver(int cacheSize) {
        this.cache = new LRUCache<>(cacheSize);
    }

    public InetAddress resolve(String host) throws UnknownHostException {
        if (host == null || host.isBlank()) {
            failures.incrementAndGet();
            throw new UnknownHostException("Tên miền rỗng");
        }

        String key = host.toLowerCase(Locale.ROOT);

        InetAddress cached = cache.get(key);
        if (cached != null) {
            hits.incrementAndGet();
            return cached;
        }

        misses.incrementAndGet();
        try {
            InetAddress resolved = InetAddress.getByName(key);
            cache.put(key, resolved);
            return resolved;
        } catch (UnknownHostException e) {
            failures.incrementAndGet();
            throw e;
        }
    }

    public InetAddress resolveHostOf(String url) throws UnknownHostException {
        return resolve(hostOf(url));
    }

    public static String hostOf(String url) {
        try {
            return URI.create(url).getHost();
        } catch (Exception e) {
            return null;
        }
    }

    public long getCacheHits() {
        return hits.get();
    }

    public long getCacheMisses() {
        return misses.get();
    }

    public long getResolveFailures() {
        return failures.get();
    }

    public int getCachedHostCount() {
        return cache.size();
    }

    /** Tỷ lệ trúng cache trong khoảng [0, 1]; trả về 0 khi chưa có lượt tra nào. */
    public double hitRate() {
        long total = hits.get() + misses.get();
        return total == 0 ? 0.0 : (double) hits.get() / total;
    }

    public static void main(String[] args) throws Exception {
        DnsResolver resolver = new DnsResolver();
        System.out.println("Lần 1 (trượt cache): " + resolver.resolve("vnexpress.net"));
        System.out.println("Lần 2 (trúng cache): " + resolver.resolve("vnexpress.net"));
        System.out.println("Số lượt trúng : " + resolver.getCacheHits());
        System.out.println("Số lượt trượt : " + resolver.getCacheMisses());
        System.out.printf("Tỷ lệ trúng   : %.0f%%%n", resolver.hitRate() * 100);
    }

}