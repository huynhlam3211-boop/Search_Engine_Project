package com.vnsearch.crawler;

import com.vnsearch.datastructure.BloomFilter;

public class UrlSeenFilter {

    public static final int URLS_SEEN_PER_PAGE = 200;
    public static final int MIN_EXPECTED_URLS = 200000;

    public static UrlSeenFilter forMaxPages(int maxPages) {
        return forMaxPages(maxPages, UrlStorage.disabled());
    }

    public static UrlSeenFilter forMaxPages(int maxPages, UrlStorage urlStorage){

    }

    public long replayedFromStorage() {
        
    }

    public boolean markSeenIfNew(String url) {

    }
}
