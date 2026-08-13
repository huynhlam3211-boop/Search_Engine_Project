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
            throw new IllegalArgumentException("random không được null");
        }
        this.random = random;
    }

    @Override
    public int select(int[] queueSizes) {
        int levels = queueSizes.length;
        if (levels > MAX_LEVELS) {
            throw new IllegalArgumentException(
                    "Chỉ hỗ trợ tối đa " + MAX_LEVELS + " mức, nhận được: " + levels);
        }

        // Lượt 1: cộng trọng số của các hàng đợi CÒN HÀNG.
        long totalWeight = 0;
        for (int i = 0; i < levels; i++) {
            if (queueSizes[i] > 0) {
                totalWeight += weightOf(i, levels);
            }
        }
        if (totalWeight == 0) {
            return -1; // mọi hàng đợi đều rỗng
        }

        // Lượt 2: bốc một điểm trong [0, totalWeight) rồi đi tới khi vượt qua nó.
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
        // Không tới được: tổng trọng số đã tính đúng ở lượt 1.
        throw new IllegalStateException("Không chọn được hàng đợi dù tổng trọng số > 0");
    }

    private static long weightOf(int level, int levels) {
        return 1L << (levels - 1 - level);
    }

    /** Demo minh hoạ nhỏ để chụp màn hình làm báo cáo. */
    public static void main(String[] args) {
        WeightedRandomSelector selector = new WeightedRandomSelector();
        int[] sizes = {10, 10, 10, 10, 10};
        int[] hits = new int[sizes.length];
        for (int i = 0; i < 100_000; i++) {
            hits[selector.select(sizes)]++;
        }
        System.out.println("Phân bố 100.000 lượt chọn trên 5 mức (cả 5 đều còn URL):");
        for (int i = 0; i < hits.length; i++) {
            System.out.printf("  mức %d: %5.2f%%  (lý thuyết %5.2f%%)%n",
                    i, hits[i] / 1000.0, weightOf(i, hits.length) * 100.0 / 31);
        }
    }
}