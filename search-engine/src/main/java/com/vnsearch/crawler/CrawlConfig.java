package com.vnsearch.crawler;
import java.util.Set;

public class CrawlConfig {
    private final int maxDepth;
    private final int maxPages;
    private final int threadCount;
    private final Set<String> allowedDomains;
    private final Set<String> excludeHostPrefixes;
    private final int maxDurationMinutes;
    private final String urlStoragePath;

    private CrawlConfig(Builder builder) {
        this.maxDepth = builder.maxDepth;
        this.maxPages = builder.maxPages;
        this.threadCount = builder.threadCount;
        this.allowedDomains = Set.copyOf(builder.allowedDomains);
        this.excludeHostPrefixes = Set.copyOf(builder.excludeHostPrefixes);
        this.maxDurationMinutes = builder.maxDurationMinutes;
        this.urlStoragePath = builder.urlStoragePath;
    }

    public static Builder builder(){
        return new Builder();
    }

    public int maxDepth(){
        return maxDepth;
    }

    public int maxPages(){
        return maxPages;
    }

    public int threadCount(){
        return threadCount;
    }

    public Set<String> excludeHostPrefixes(){
        return excludeHostPrefixes;
    }

    public Set<String> allowedDomains() {
        return allowedDomains;
    }

    public int maxDurationMinutes(){
        return maxDurationMinutes;
    }

    public String urlStoragePath() {
        return urlStoragePath;
    }

    @Override
    public String toString(){
        return "CrawlConfig{maxDepth=" + maxDepth + ", maxPages=" + maxPages
                + ", threadCount=" + threadCount + ", domains=" + allowedDomains.size()
                + ", maxDurationMinutes=" + maxDurationMinutes + "}";
    }

    public static final class Builder {
        private int maxDepth = 3;
        private int maxPages = 100;
        private int threadCount = 4;
        private Set<String> allowedDomains = Set.of();
        private Set<String> excludeHostPrefixes = Set.of();
        private int maxDurationMinutes = 60;
        private String urlStoragePath = null;

        private Builder() {
        }

        public Builder maxDepth(int value) {
            this.maxDepth = value;
            return this;
        }

        public Builder maxPages(int value){
            this.maxPages = value;
            return this;
        }

        public Builder threadCount(int value){
            this.threadCount = value;
            return this;
        }

        public Builder allowedDomains(Set<String> value) {
            this.allowedDomains = value == null ? Set.of() : value;
            return this;
        }

        public Builder excludeHostPrefixes(Set<String> value) {
            this.excludedHostPrefixes = value == null ? Set.of() : value;
            return this;
        }

        public Builder maxDurationMinutes(int value) {
            this.maxDurationMinutes = value;
            return this;
        }

        public Builder urlStoragePath(String value) {
            this.urlStoragePath = value == null || value.isBlank() ? null : value;
            return this;
        }

        public CrawlConfig build() {
            if (maxPages <= 0) {
                throw new IllegalArgumentException("maxPages must be > 0, " + maxPages);
            }
            if (maxDepth < 0) {
                throw new IllegalArgumentException("maxDepth must be >= 0, " + maxDepth);
            }
            if (threadCount <= 0) {
                throw new IllegalArgumentException("threadCount must be > 0," + threadCount);
            }
            if (maxDurationMinutes <= 0) {
                throw new IllegalArgumentException(
                        "maxDurationMinutes must be > 0," + maxDurationMinutes);
            }
            return new CrawlConfig(this);
        }
    }
}
