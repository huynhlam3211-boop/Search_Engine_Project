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

    }

    public InetAddress resolveHostOf(String url) thrown UnknownHostExceoption {

    }

    public static String hostOf(String url) {

    }

    public long getCacheHits() {

    }

    public long getCacheMisses() {

    }

    public long getResolveFailures() {

    }

    public int getCachedHostCount() {

    }

    public double hitRate() {

    }

    public static void main(String[] args) throws Exception {
        
    }

}