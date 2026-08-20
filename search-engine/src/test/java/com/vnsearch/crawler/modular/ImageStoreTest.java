package com.vnsearch.crawler.modular;

import com.vnsearch.crawler.bus.ImageFound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageStoreTest {

    private ImageStore store;

    @BeforeEach
    void setUp() {
        store = new ImageStore();
    }

    private static ImageFound image(String pageUrl, String imageUrl) {
        return ImageFound.metadataOnly(pageUrl, "a.vn", imageUrl, "mo ta", 800, 600);
    }

    private static ImageFound sized(String pageUrl, String imageUrl, int width) {
        return ImageFound.metadataOnly(pageUrl, "a.vn", imageUrl, "mo ta", width, 600);
    }

    @Test
    void storesOneImagePerPage() {
        assertTrue(store.add(image("https://a.vn/bai", "https://a.vn/1.jpg")));

        assertEquals(1, store.forPage("https://a.vn/bai").size());
        assertEquals(1, store.pageCount());
        assertEquals(1, store.imageCount());
    }

    @Test
    void unknownPageGivesEmptyListNotNull() {
        assertEquals(List.of(), store.forPage("https://a.vn/khong-co"));
    }

    @Test
    void aSecondImageNeverGrowsThePage() {
        store.add(sized("https://a.vn/bai", "https://a.vn/nho.jpg", 300));
        store.add(sized("https://a.vn/bai", "https://a.vn/to.jpg", 1200));
        store.add(sized("https://a.vn/bai", "https://a.vn/vua.jpg", 700));

        assertEquals(1, store.forPage("https://a.vn/bai").size());
        assertEquals(1, store.imageCount());
    }

    @Test
    void keepsTheWiderImage() {
        store.add(sized("https://a.vn/bai", "https://a.vn/nho.jpg", 300));
        store.add(sized("https://a.vn/bai", "https://a.vn/to.jpg", 1200));

        assertEquals("https://a.vn/to.jpg", store.forPage("https://a.vn/bai").get(0).imageUrl());
    }

    @Test
    void resultDoesNotDependOnArrivalOrder() {
        ImageStore xuoi = new ImageStore();
        xuoi.add(sized("https://a.vn/b", "https://a.vn/nho.jpg", 300));
        xuoi.add(sized("https://a.vn/b", "https://a.vn/to.jpg", 1200));

        ImageStore nguoc = new ImageStore();
        nguoc.add(sized("https://a.vn/b", "https://a.vn/to.jpg", 1200));
        nguoc.add(sized("https://a.vn/b", "https://a.vn/nho.jpg", 300));

        assertEquals(xuoi.forPage("https://a.vn/b").get(0).imageUrl(),
                nguoc.forPage("https://a.vn/b").get(0).imageUrl());
    }

    @Test
    void aLogoLosesToAnArticlePhoto() {
        store.add(ImageFound.metadataOnly(
                "https://a.vn/bai", "a.vn", "https://a.vn/logo.png", "Fica", 100, 42));
        store.add(ImageFound.metadataOnly(
                "https://a.vn/bai", "a.vn", "https://a.vn/anh-bai.jpg", "Anh bai viet", 900, 600));

        assertEquals("https://a.vn/anh-bai.jpg", store.forPage("https://a.vn/bai").get(0).imageUrl());
    }

    @Test
    void svgLosesToAPhotoEvenWithoutDeclaredSize() {
        store.add(ImageFound.metadataOnly(
                "https://a.vn/bai", "a.vn", "https://a.vn/bieu-tuong.svg", "icon", -1, -1));
        store.add(ImageFound.metadataOnly(
                "https://a.vn/bai", "a.vn", "https://a.vn/anh.jpg", "", -1, -1));

        assertEquals("https://a.vn/anh.jpg", store.forPage("https://a.vn/bai").get(0).imageUrl());
    }

    @Test
    void keepsADecorativeImageWhenItIsTheOnlyOne() {
        store.add(ImageFound.metadataOnly(
                "https://a.vn/bai", "a.vn", "https://a.vn/logo.svg", "", -1, -1));

        assertEquals(1, store.forPage("https://a.vn/bai").size());
    }

    @Test
    void forPagesKeepsThePageOrder() {
        store.add(image("https://a.vn/hai", "https://a.vn/2.jpg"));
        store.add(image("https://a.vn/mot", "https://a.vn/1.jpg"));

        List<ImageFound> images =
                store.forPages(List.of("https://a.vn/mot", "https://a.vn/hai"), 10);

        assertEquals(2, images.size());
        assertEquals("https://a.vn/1.jpg", images.get(0).imageUrl());
        assertEquals("https://a.vn/2.jpg", images.get(1).imageUrl());
    }

    @Test
    void forPagesShowsARepeatedImageOnlyOnce() {
        store.add(image("https://a.vn/mot", "https://a.vn/chung.jpg"));
        store.add(image("https://a.vn/hai", "https://a.vn/chung.jpg"));

        assertEquals(1, store.forPages(List.of("https://a.vn/mot", "https://a.vn/hai"), 10).size());
    }

    @Test
    void repeatedReadsGiveTheSameOrder() {
        for (int i = 0; i < 20; i++) {
            store.add(image("https://a.vn/trang-" + i, "https://a.vn/" + i + ".jpg"));
        }
        List<String> pages = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            pages.add("https://a.vn/trang-" + i);
        }

        List<ImageFound> lan1 = store.forPages(pages, 100);
        for (int lap = 0; lap < 5; lap++) {
            assertEquals(lan1, store.forPages(pages, 100), "Moi lan doc phai cho cung mot ket qua");
        }
    }
    @Test
    void consecutiveSlicesCoverEverythingExactlyOnce() {
        List<String> pages = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            store.add(image("https://a.vn/trang-" + i, "https://a.vn/" + i + ".jpg"));
            pages.add("https://a.vn/trang-" + i);
        }
        List<ImageFound> tatCa = store.forPages(pages, 100);
        assertEquals(25, tatCa.size());

        java.util.Set<String> gom = new java.util.LinkedHashSet<>();
        for (int from = 0; from < tatCa.size(); from += 8) {
            int to = Math.min(from + 8, tatCa.size());
            tatCa.subList(from, to).forEach(img -> gom.add(img.imageUrl()));
        }
        assertEquals(25, gom.size(), "Cac lat phai phu het va khong trung nhau");
    }

    @Test
    void forPagesRespectsTheLimit() {
        List<String> pages = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            store.add(image("https://a.vn/trang-" + i, "https://a.vn/" + i + ".jpg"));
            pages.add("https://a.vn/trang-" + i);
        }
        assertEquals(3, store.forPages(pages, 3).size());
        assertEquals(0, store.forPages(pages, 0).size());
        assertEquals(0, store.forPages(null, 5).size());
    }

    @Test
    void addAllCollapsesAnOldMultiImageFile() {
        List<ImageFound> cu = List.of(
                sized("https://a.vn/b", "https://a.vn/1.jpg", 200),
                sized("https://a.vn/b", "https://a.vn/2.jpg", 1400),
                sized("https://a.vn/b", "https://a.vn/3.jpg", 500),
                sized("https://a.vn/khac", "https://a.vn/4.jpg", 900));

        store.addAll(cu);

        assertEquals(2, store.pageCount());
        assertEquals(2, store.all().size());
        assertEquals("https://a.vn/2.jpg", store.forPage("https://a.vn/b").get(0).imageUrl());
    }

    @Test
    void nullImageIsIgnored() {
        assertFalse(store.add(null));
        assertEquals(0, store.imageCount());
        assertEquals(0, store.addAll(null));
    }

    @Test
    void clearEmptiesTheStore() {
        store.add(image("https://a.vn/bai", "https://a.vn/1.jpg"));
        store.clear();
        assertEquals(0, store.pageCount());
        assertEquals(List.of(), store.forPage("https://a.vn/bai"));
    }

    @Test
    void snapshotReportsTheHeadlineNumbers() {
        store.add(sized("https://a.vn/bai", "https://a.vn/nho.jpg", 300));
        store.add(sized("https://a.vn/bai", "https://a.vn/to.jpg", 1200));   // thay the
        store.add(sized("https://a.vn/bai", "https://a.vn/vua.jpg", 700));   // bi bo

        Map<String, Object> snapshot = store.snapshot();
        assertEquals(1, snapshot.get("pagesWithImages"));
        assertEquals(1, snapshot.get("images"));
        assertEquals(1L, snapshot.get("replaced"));
        assertEquals(1L, snapshot.get("candidatesRejected"));
    }

    @Test
    void isThreadSafeUnderConcurrentWrites() throws Exception {
        int threads = 8;
        int perThread = 200;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch done = new CountDownLatch(threads);
        List<Throwable> loi = Collections.synchronizedList(new ArrayList<>());

        for (int t = 0; t < threads; t++) {
            final int id = t;
            pool.submit(() -> {
                try {
                    for (int i = 0; i < perThread; i++) {
                        store.add(sized("https://a.vn/chung", "https://a.vn/" + id + "-" + i + ".jpg",
                                200 + id * perThread + i));
                        store.add(image("https://a.vn/rieng-" + id, "https://a.vn/r" + id + "-" + i + ".jpg"));
                    }
                } catch (Throwable e) {
                    loi.add(e);
                } finally {
                    done.countDown();
                }
            });
        }

        assertTrue(done.await(30, TimeUnit.SECONDS));
        pool.shutdownNow();
        assertEquals(List.of(), loi);

        assertEquals(threads + 1, store.pageCount());
        assertEquals(threads + 1, store.all().size());

        int rongNhat = 200 + (threads - 1) * perThread + (perThread - 1);
        assertEquals(rongNhat, store.forPage("https://a.vn/chung").get(0).declaredWidth());
    }

    @Test
    void respectsThePageLimit() {
        assertEquals(0, store.getDroppedByPageLimitCount());
    }
}
