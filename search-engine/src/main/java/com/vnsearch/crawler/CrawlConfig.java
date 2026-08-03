package com.vnsearch.crawler;


public class CrawlConfig {
    private final int maxDepth;
    private final int maxPages;
    private final int threadCount;
    private final Set<String> allowedDomains;
    private final int maxDurationMinutes;
    private final String urlStoragePath;

    private CrawlConfig(Builder builder) {

    }

    public static Builder builder(){
        return new Builder();
    }

    public static final class Builder() {
        private int maxDepth = 3;
        private int maxPages = 100;
        private int threadCount = 4;
        private Set<String> allowedDomains = Set.of();
        private int maxDurationMinutes = 60;
        private String urlStoragePath = null;

        private Builder() {
        }
    }
}
