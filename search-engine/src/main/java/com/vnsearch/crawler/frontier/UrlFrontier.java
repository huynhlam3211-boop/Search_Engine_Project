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

    /** Chống xếp hàng trùng: chính xác tuyệt đối, khác với Bloom Filter ở tầng crawler. */
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

    /**
     * @param maxSize        số URL đang chờ tối đa
     * @param prioritizer    chính sách xếp mức ưu tiên
     * @param selector       chính sách chọn hàng đợi trước
     * @param backQueueCount số host được phép hoạt động cùng lúc
     */

    public UrlFrontier(int maxSize, Prioritizer prioritizer, FrontQueueSelector selector,
                        int backQueueCount) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize phải > 0, nhận được: " + maxSize);
        }
        if (prioritizer == null) {
            throw new IllegalArgumentException("prioritizer không được null");
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
        // Phân tích URL một lần duy nhất, ngoài khối khoá; host đi theo
        // CrawlTask nên cả prioritizer lẫn tầng sau không phải phân tích lại.
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
            frontQueues.add(task, level);       // Prioritizer -> f1..fn
            enqueued.add(url);
            pendingPerHost.merge(task.host(), 1, Integer::sum);
            totalSize++;
            return true;
        }
    }

    /**
     * Lấy URL kế tiếp nên crawl.
     *
     * <p>Blocking: nếu frontier còn URL nhưng mọi host đều đang trong thời
     * gian hoãn, luồng sẽ ngủ rồi thử lại. Trả về {@code null} khi hàng đợi
     * thật sự rỗng — tín hiệu để crawler cân nhắc dừng.
     */
    public CrawlTask nextUrl() {
        while (true) {
            long sleepMs;
            synchronized (lock) {
                if (totalSize == 0) {
                    return null;
                }
                backQueues.refillFrom(frontQueues);          // Back queue router

                long now = System.currentTimeMillis();
                CrawlTask task = backQueues.poll(now);       // Back queue selector
                if (task != null) {
                    enqueued.remove(task.url());
                    releaseHost(task.host());
                    totalSize--;
                    return task;
                }
                sleepMs = sleepUntilNextSlot(now);
            }
            // Ngủ NGOÀI khối khoá để không chặn các luồng đang muốn addUrl.
            try {
                Thread.sleep(sleepMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
    }

    /** Ngủ vừa đủ tới lúc hàng đợi sớm nhất khả dụng, nhưng không quá {@link #MAX_SLEEP_MS}. */
    private long sleepUntilNextSlot(long now) {
        long earliest = backQueues.earliestAvailableAt();
        if (earliest == Long.MAX_VALUE) {
            return MAX_SLEEP_MS; // không hàng đợi sau nào còn hàng, chỉ chờ URL mới
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

    /** Số host phân biệt đang có URL chờ, tính trên cả hai tầng. */
    public int domainCount() {
        synchronized (lock) {
            return pendingPerHost.size();
        }
    }

    /** Số URL bị bỏ do frontier đã đầy - dùng cho thống kê/báo cáo. */
    public long getDroppedDueToCapacity() {
        synchronized (lock) {
            return droppedDueToCapacity;
        }
    }

    /** Số URL đang nằm ở tầng trước (chưa được định tuyến về host). */
    public int frontQueueSize() {
        synchronized (lock) {
            return frontQueues.size();
        }
    }

    /** Số URL đang nằm ở tầng sau (đã gán host, chờ tới lượt). */
    public int backQueueSize() {
        synchronized (lock) {
            return backQueues.pendingCount();
        }
    }


    /** Số host đang chiếm một hàng đợi sau — trần là số hàng đợi sau. */
    public int activeHostCount() {
        synchronized (lock) {
            return backQueues.boundHostCount();
        }
    }

    /** Demo minh hoạ nhỏ để chụp màn hình làm báo cáo. */
    public static void main(String[] args) {
        // Bộ chọn tất định để đầu ra của demo lặp lại được.
        UrlFrontier frontier = new UrlFrontier(DEFAULT_MAX_SIZE, new DefaultPrioritizer(),
                new StrictPrioritySelector(), DEFAULT_BACK_QUEUE_COUNT);

        frontier.addUrl("https://example.com/a", 1, 2);
        frontier.addUrl("https://congty.gov.vn/b", 1, 2); // domain .vn -> nâng một bậc ưu tiên
        frontier.addUrl("https://example.com/c", 3, 0);   // sâu hơn -> mức thấp hơn

        System.out.println("Số host đang chờ: " + frontier.domainCount());
        System.out.println("Task 1 (ưu tiên cao nhất, phải là domain .vn): " + frontier.nextUrl());
        System.out.println("Task 2: " + frontier.nextUrl());
        System.out.println("Task 3 (sâu nhất -> ưu tiên thấp nhất): " + frontier.nextUrl());
        System.out.println("Còn lại: " + frontier.size());
    }
}
