package com.vnsearch.crawler.frontier;

public final class DefaultPrioritizer implements Prioritizer {
    public static final int DEFAULT_LEVELS = 5;

    public static final int BACKLINK_BOOST_THRESHOLD = 5;

    private final int levels;

    public DefaultPrioritizer() {
        this(DEFAULT_LEVELS);
    }

    public DefaultPrioritizer(int levels) {
        if (levels <= 0) {
            throw new IllegalArgumentException("levels must be > 0, got: " + levels);
        }
        this.levels = levels;
    }

    @Override
    public int levels() {
        return levels;
    }

    @Override
    public int levelOf(String url, String host, int depth, int knownBacklinks) {
        int level = depth;
        if (host != null && host.endsWith(".vn")) {
            level--;
        }
        if (knownBacklinks >= BACKLINK_BOOST_THRESHOLD) {
            level--;
        }
        return Math.max(0, Math.min(level, levels - 1));
    }

    public static void main(String[] args) {
        DefaultPrioritizer prioritizer = new DefaultPrioritizer();
        System.out.println("Priority levels: " + prioritizer.levels() + " (0 = highest)");
        System.out.println("seed .vn          : " + prioritizer.levelOf("https://a.vn", "a.vn", 0, 10));
        System.out.println("depth 1, .vn      : " + prioritizer.levelOf("https://a.vn/x", "a.vn", 1, 0));
        System.out.println("depth 1, .com     : " + prioritizer.levelOf("https://a.com/x", "a.com", 1, 0));
        System.out.println("depth 1, backlink : " + prioritizer.levelOf("https://a.com/y", "a.com", 1, 40));
        System.out.println("depth 9           : " + prioritizer.levelOf("https://a.com/z", "a.com", 9, 0));
    }

}