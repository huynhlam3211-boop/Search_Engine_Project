package com.vnsearch.crawler;

import com.vnsearch.datastructure.BloomFilter;

public class UrlSeenFilter {

    public static final int URLS_SEEN_PER_PAGE = 200;

    public static final int MIN_EXPECTED_URLS = 200000;
    public static final int MAX_EXPECTED_URL = 50_000_000;

    private static final double FALSE_POSITIVE_RATE = 0.01;

    private final BloomFilter bloomFilter;
    private final UrlStorage urlStorage;
    private final Object lock = new Object();

    private long seenCount;

    public static UrlSeenFilter forMaxPages(int maxPages) {
        return forMaxPages(maxPages, UrlStorage.disabled());
    }

    
    public static UrlSeenFilter forMaxPages(int maxPages, UrlStorage urlStorage){
        long expected = Math.max(MIN_EXPECTED_URLS, (long) maxPages * URLS_SEEN_PER_PAGE);
        return new UrlSeenFilter((int) Math.min(expected, MAX_EXPECTED_URLS), urlStorage);
    }

    public UrlSeenFilter(int expectedUrl, UrlStorage urlStorage){
        this.bloomFilter = new BloomFilter(expectedUrl, FALSE_POSITIVE_RATE);
        this.urlStorage = urlStorage == null ? UrlStorage.disabled() : urlStorage;
    }

    
    public long replayedFromStorage() {
        return urlStorage.replay( url -> {
            synchronized (lock) {
                if (!bloomFilter.mightContain(url)) {
                    bloomFilter.add(url);
                    seenCount++;
                }
            }
        });
    }

    public boolean markSeenIfNew(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        synchronized (lock) {
            if (bloomFilter.mightContain(url)) {
                return false;
            }
            bloomFilter.add(url);
            seenCount++;
            urlStorage.append(url);
            return true;
        }
    }

    public boolean seenBefore(String url) {
        if (url == null || url.isBlank()) {
            return true;
        }
        synchronized (lock) {
            return bloomFilter.mightContain(url);
        }
    }

    public long replayFromStorage() {
        return urlStorage.replay(url -> {
            synchronized (lock) {
                if (!bloomFilter.mightContain(url)) {
                    bloomFilter.add(url);
                    seenCount++;
                }
            }
        });
    }

    public long getSeenCount() {
        synchronized (lock) {
            return seenCount;
        }
    }

    public int getNumBits() {
        return bloomFilter.getNumBits();
    }

    public int getNumHashes() {
        return bloomFilter.getNumHashes();
    }

    public UrlStorage getUrlStorage() {
        return urlStorage;
    }
}
