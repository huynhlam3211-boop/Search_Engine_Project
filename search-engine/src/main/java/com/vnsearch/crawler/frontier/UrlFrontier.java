package com.vnsearch.crawler.frontier;

import com.vnsearch.crawler.UrlCanonicalizer;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UrlFrontier {

    public static final long POLITENESS_DELAY_MS = 1000L;

    public static final int DEFAULT_MAX_SIZE = 500_000;

    public static final int DEFAULT_BACK_QUEUE_COUNT = 128;

    private static final long MAX_SLEEP_MS = 50L;

    private final Prioritizer prioritizer;
    private final FrontQueues frontQueues;
    private final BackQueues backQueues;

    private final Set<String> enqueued = new HashSet<>();

    private final Object lock = new Object();
    private final int maxSize;

    private int totalSize;
    private long droppedDueToCapacity;

    public UrlFrontier() {
        this(DEFAULT_MAX_SIZE);
    }

    public UrlFrontier(int maxSize) {
        this(maxSize, new DefaultPrioritizer(), new WeightedRandomSelector(),
                DEFAULT_BACK_QUEUE_COUNT);
    }

    public UrlFrontier(int maxSize, Prioritizer prioritizer, FrontQueueSelector selector,
                        int backQueueCount) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be > 0, got: " + maxSize);
        }
        if (prioritizer == null) {
            throw new IllegalArgumentException("prioritizer must not be null");
        }
        this.maxSize = maxSize;
        this.prioritizer = prioritizer;
        this.frontQueues = new FrontQueues(prioritizer.levels(), selector);
        this.backQueues = new BackQueues(backQueueCount, POLITENESS_DELAY_MS);
    }

    public boolean addUrl(String rawUrl, int depth, int knownBacklinks) {
        String url = UrlCanonicalizer.canonicalize(rawUrl);
        if (url == null || url.isBlank()) {
            return false;
        }
        String host = hostOf(url);
        CrawlTask task = new CrawlTask(url, host != null ? host : url, depth);
        int level = prioritizer.levelOf(url, task.host(), depth, knownBacklinks);

        synchronized (lock) {
            if (enqueued.contains(url)) {
                return false;
            }
            if (totalSize >= maxSize) {
                droppedDueToCapacity++;
                return false;
            }
            frontQueues.add(task, level);
            enqueued.add(url);
            pendingPerHost.merge(task.host(), 1, Integer::sum);
            totalSize++;
            return true;
        }
    }

    public CrawlTask nextUrl() {
        while (true) {
            long sleepMs;
            synchronized (lock) {
                if (totalSize == 0) {
                    return null;
                }
                backQueues.refillFrom(frontQueues);

                long now = System.currentTimeMillis();
                CrawlTask task = backQueues.poll(now);
                if (task != null) {
                    enqueued.remove(task.url());
                    releaseHost(task.host());
                    totalSize--;
                    return task;
                }
                sleepMs = sleepUntilNextSlot(now);
            }
            try {
                Thread.sleep(sleepMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
    }

    private long sleepUntilNextSlot(long now) {
        long earliest = backQueues.earliestAvailableAt();
        if (earliest == Long.MAX_VALUE) {
            return MAX_SLEEP_MS;
        }
        return Math.min(MAX_SLEEP_MS, Math.max(1L, earliest - now));
    }

    private void releaseHost(String host) {
        pendingPerHost.computeIfPresent(host, (key, count) -> count == 1 ? null : count - 1);
    }

    private static String hostOf(String url) {
        try {
            return URI.create(url).getHost();
        } catch (Exception e) {
            return null;
        }
    }

    public int size() {
        synchronized (lock) {
            return totalSize;
        }
    }

    public boolean isEmpty() {
        synchronized (lock) {
            return totalSize == 0;
        }
    }

    public int domainCount() {
        synchronized (lock) {
            return pendingPerHost.size();
        }
    }

    public long getDroppedDueToCapacity() {
        synchronized (lock) {
            return droppedDueToCapacity;
        }
    }

    public int frontQueueSize() {
        synchronized (lock) {
            return frontQueues.size();
        }
    }

    public int backQueueSize() {
        synchronized (lock) {
            return backQueues.pendingCount();
        }
    }

    public int activeHostCount() {
        synchronized (lock) {
            return backQueues.boundHostCount();
        }
    }

    public static void main(String[] args) {
        UrlFrontier frontier = new UrlFrontier(DEFAULT_MAX_SIZE, new DefaultPrioritizer(),
                new StrictPrioritySelector(), DEFAULT_BACK_QUEUE_COUNT);

        frontier.addUrl("https://example.com/a", 1, 2);
        frontier.addUrl("https://company.gov.vn/b", 1, 2);
        frontier.addUrl("https://example.com/c", 3, 0);

        System.out.println("Pending hosts: " + frontier.domainCount());
        System.out.println("Task 1 (highest priority, must be a .vn domain): " + frontier.nextUrl());
        System.out.println("Task 2: " + frontier.nextUrl());
        System.out.println("Task 3 (deepest -> lowest priority): " + frontier.nextUrl());
        System.out.println("Remaining: " + frontier.size());
    }
}
