package com.vnsearch.crawler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckpointCrawlListenerTest {

    private static final int EVERY_N = 250;

    @Test
    @DisplayName("Lan ghi dau tien luon duoc phep")
    void firstCheckpointIsAlwaysAllowed() {
        assertTrue(CheckpointCrawlListener.isDueForCheckpoint(250, 0, EVERY_N));
    }

    @Test
    @DisplayName("Corpus con nho: van ghi deu moi everyN trang")
    void writesEveryNWhileCorpusIsSmall() {.
        assertTrue(CheckpointCrawlListener.isDueForCheckpoint(750, 500, EVERY_N));
        assertFalse(CheckpointCrawlListener.isDueForCheckpoint(700, 500, EVERY_N));
    }

    @Test
    @DisplayName("Corpus lon: nguong gian theo 25% kich thuoc hien tai")
    void thresholdGrowsWithCorpusSize() {
        assertFalse(CheckpointCrawlListener.isDueForCheckpoint(20_250, 20_000, EVERY_N));
        assertFalse(CheckpointCrawlListener.isDueForCheckpoint(24_000, 20_000, EVERY_N));
        assertTrue(CheckpointCrawlListener.isDueForCheckpoint(25_000, 20_000, EVERY_N));
    }

    @Test
    @DisplayName("Ca phien 30.000 trang chi ghi khoang 20 lan, khong phai 120 lan")
    void wholeSessionWritesFarFewerTimes() {
        int lastCheckpoint = 0;
        int writes = 0;

        for (int pages = EVERY_N; pages <= 30_000; pages += EVERY_N) {
            if (CheckpointCrawlListener.isDueForCheckpoint(pages, lastCheckpoint, EVERY_N)) {
                lastCheckpoint = pages;
                writes++;
            }
        }

        assertTrue(writes < 30, "Phai it hon 30 lan ghi, thuc te: " + writes);

        assertTrue(writes > 10, "Phai nhieu hon 10 lan ghi, thuc te: " + writes);

        assertTrue(lastCheckpoint >= 24_000,
                "Diem kiem tra cuoi qua xa cuoi phien: " + lastCheckpoint);
    }
}
