package com.vnsearch.crawler.frontier;

import java.util.Random;

public final class WeightedRandomSelector implements FrontQueueSelector {
    public static final long DEFAULT_SEED = 20240801L;

    private static final int MAX_LEVELS = 30;

    private final Random random;

    public WeightedRandomSelector() {
        this(DEFAULT_SEED);
    }

    public WeightedRandomSelector(long seed) {
        this(new Random(seed));
    }

    public WeightedRandomSelector(Random random) {
        if (random == null) {
            throw new IllegalArgumentException("random must not be null");
        }
        this.random = random;
    }

    @Override
    public int select(int[] queueSizes) {
        int levels = queueSizes.length;
        if (levels > MAX_LEVELS) {
            throw new IllegalArgumentException(
                    "Supports at most " + MAX_LEVELS + " levels, got: " + levels);
        }

        long totalWeight = 0;
        for (int i = 0; i < levels; i++) {
            if (queueSizes[i] > 0) {
                totalWeight += weightOf(i, levels);
            }
        }
        if (totalWeight == 0) {
            return -1;
        }

        long pick = Math.floorMod(random.nextLong(), totalWeight);
        for (int i = 0; i < levels; i++) {
            if (queueSizes[i] == 0) {
                continue;
            }
            pick -= weightOf(i, levels);
            if (pick < 0) {
                return i;
            }
        }
        throw new IllegalStateException("Failed to pick a queue even though total weight > 0");
    }

    private static long weightOf(int level, int levels) {
        return 1L << (levels - 1 - level);
    }

    public static void main(String[] args) {
        WeightedRandomSelector selector = new WeightedRandomSelector();
        int[] sizes = {10, 10, 10, 10, 10};
        int[] hits = new int[sizes.length];
        for (int i = 0; i < 100_000; i++) {
            hits[selector.select(sizes)]++;
        }
        System.out.println("Distribution of 100,000 picks across 5 levels (all 5 still have URLs):");
        for (int i = 0; i < hits.length; i++) {
            System.out.printf("  level %d: %5.2f%%  (theoretical %5.2f%%)%n",
                    i, hits[i] / 1000.0, weightOf(i, hits.length) * 100.0 / 31);
        }
    }
}