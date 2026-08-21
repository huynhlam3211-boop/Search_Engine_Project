package com.vnsearch.crawler;

import com.vnsearch.crawler.bus.ImageFound;
import com.vnsearch.crawler.modular.ImageStorage;
import com.vnsearch.model.WebDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public final class CheckpointCrawlListener implements CrawlListener {
    private static final Logger log = LoggerFactory.getLogger(CheckpointCrawlListener.class);
    private static final double GROWTH_RATIO = 0.25;
    private final Supplier<List<WebDocument>> snapshot;

    private final Supplier<List<ImageFound>> imageSnapshot;

    private final String path;
    private final int everyN;

    private final AtomicBoolean writing = new AtomicBoolean();

    private final ExecutorService writer = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "crawl-checkpoint");
        t.setDaemon(true);
        return t;
    });

    private volatile int lastCheckpointPages;

    /**
     * @param snapshot nguồn lấy bản chụp corpus tại thời điểm ghi
     * @param path     tệp đích — TRÙNG với tệp đầu ra cuối phiên, để lần chạy
     *                 sau nạp lại được mà không cần biết có điểm kiểm tra hay không
     * @param everyN   ghi sau mỗi bấy nhiêu trang
     */
    public CheckpointCrawlListener(Supplier<List<WebDocument>> snapshot, String path, int everyN) {
        this(snapshot, null, path, everyN);
    }

    public CheckpointCrawlListener(Supplier<List<WebDocument>> snapshot,
                                   Supplier<List<ImageFound>> imageSnapshot,
                                   String path, int everyN) {
        this.snapshot = snapshot;
        this.imageSnapshot = imageSnapshot;
        this.path = path;
        this.everyN = Math.max(1, everyN);
    }

    @Override
    public void onPageCrawled(CrawlEvent e) {
        if (e.pageNumber() % everyN != 0
                || !isDueForCheckpoint(e.pageNumber(), lastCheckpointPages, everyN)) {
            return;
        }
        if (!writing.compareAndSet(false, true)) {
            log.debug("Bỏ qua điểm kiểm tra ở trang {}: lần ghi trước chưa xong", e.pageNumber());
            return;
        }
        int pages = e.pageNumber();
        writer.submit(() -> {
            try {
                write(pages);
            } finally {
                writing.set(false);
            }
        });
    }

    @Override
    public void onFinished(int totalPages, long elapsedMs) {
        writer.shutdown();
        try {
            if (!writer.awaitTermination(2, TimeUnit.MINUTES)) {
                log.warn("Điểm kiểm tra cuối chưa ghi xong sau 2 phút, bỏ dở");
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    static boolean isDueForCheckpoint(int pages, int lastCheckpoint, int everyN) {
        int grown = pages - lastCheckpoint;
        return grown >= Math.max(everyN, (int) (lastCheckpoint * GROWTH_RATIO));
    }

    private void write(int pages) {
        try {
            long start = System.currentTimeMillis();
            List<WebDocument> docs = snapshot.get();
            ContentStorage.saveToJson(docs, path);

            int images = -1;
            if (imageSnapshot != null) {
                List<ImageFound> snapshotImages = imageSnapshot.get();
                ImageStorage.saveToJson(snapshotImages, ImageStorage.pathFor(path));
                images = snapshotImages.size();
            }

            lastCheckpointPages = pages;
            log.info("Điểm kiểm tra: {} tài liệu{} -> {} ({} ms)",
                    docs.size(),
                    images >= 0 ? " + " + images + " ảnh" : "",
                    path, System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.warn("Không ghi được điểm kiểm tra vào {}: {}", path, e.toString());
        }
    }

    public int getLastCheckpointPages() {
        return lastCheckpointPages;
    }

}