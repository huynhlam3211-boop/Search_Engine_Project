package com.vnsearch.crawler.frontier;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class FrontQueues {

    private final List<Deque<CrawlTask>> queues;
    private final FrontQueueSelector selector;

    private final int[] sizes;

    public FrontQueues(int levels, FrontQueueSelector selector) {
        if (levels <= 0) {
            throw new IllegalArgumentException("levels must be > 0, got: " + levels);
        }
        if (selector == null) {
            throw new IllegalArgumentException("selector must not be null");
        }
        this.queues = new ArrayList<>(levels);
        for (int i = 0; i < levels; i++) {
            queues.add(new ArrayDeque<>());
        }
        this.selector = selector;
        this.sizes = new int[levels];
    }

    public void add(CrawlTask task, int level) {
        if (level < 0 || level >= queues.size()) {
            throw new IllegalArgumentException(
                    "level must be in [0, " + queues.size() + "), got: " + level);
        }
        queues.get(level).addLast(task);
        sizes[level]++;
        total++;
    }

    public CrawlTask poll() {
        if (total == 0) {
            return null;
        }
        int level = selector.select(sizes);
        if (level < 0) {
            return null;
        }
        CrawlTask task = queues.get(level).pollFirst();
        if (task == null) {
            throw new IllegalStateException("Selector returned an empty queue at level " + level);
        }
        sizes[level]--;
        total--;
        return task;
    }

    public boolean isEmpty() {
        return total == 0;
    }

    public int size() {
        return total;
    }

    public int levels() {
        return queues.size();
    }

    public int sizeOfLevel(int level) {
        return sizes[level];
    }
}