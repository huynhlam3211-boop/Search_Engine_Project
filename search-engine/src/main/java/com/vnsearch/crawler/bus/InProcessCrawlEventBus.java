package com.vnsearch.crawler.bus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class InProcessCrawlEventBus implements CrawlEventBus {

    private static final Logger log = LoggerFactory.getLogger(InProcessCrawlEventBus.class);
    
    private final List<PageEventHandler> pageHandlers = new CopyOnWriteArrayList<>();
    private final List<Consumer<DiscoveredUrl>> urlHandlers = new CopyOnWriteArrayList<>();
    private final List<Consumer<OutlinksExtracted>> outlinkHandlers = new CopyOnWriteArrayList<>();
    private final List<Consumer<ImageFound>> imageHandlers = new CopyOnWriteArrayList<>();

    private final AtomicLong publishFailures = new AtomicLong();
    private final AtomicLong pagesPublished = new AtomicLong();
    private final AtomicLong urlsPublished = new AtomicLong();
    private final AtomicLong imagesPublished = new AtomicLong();

    /** Đăng ký một Modular Service nhận luồng trang. */
    public InProcessCrawlEventBus subscribePages(PageEventHandler handler) {
        if (handler != null) {
            pageHandlers.add(handler);
        }
        return this;
    }

    /** Đăng ký bên nạp URL vào frontier — thường là {@code CrawlerService} tự nó. */
    public InProcessCrawlEventBus subscribeDiscoveredUrls(Consumer<DiscoveredUrl> handler) {
        if (handler != null) {
            urlHandlers.add(handler);
        }
        return this;
    }

    /** Đăng ký bên ghi outlinks vào Content Storage. */
    public InProcessCrawlEventBus subscribeOutlinks(Consumer<OutlinksExtracted> handler) {
        if (handler != null) {
            outlinkHandlers.add(handler);
        }
        return this;
    }

    /** Đăng ký bên lưu bản ghi ảnh. */
    public InProcessCrawlEventBus subscribeImages(Consumer<ImageFound> handler) {
        if (handler != null) {
            imageHandlers.add(handler);
        }
        return this;
    }

    @Override
    public void publishPage(PageEvent event) {
        if (event == null) {
            return;
        }
        pagesPublished.incrementAndGet();
        for (PageEventHandler handler : pageHandlers) {
            try {
                handler.onPage(event);
            } catch (Exception e) {
                publishFailures.incrementAndGet();
                // Ghi URL chứ KHÔNG ghi cả sự kiện: toString() của PageEvent đã
                // cố tình bỏ HTML, nhưng ghi rõ url ở đây vẫn dễ đọc hơn khi
                // dò log, và không có đường nào để 80 KB lọt vào tệp log.
                log.warn("Modular Service {} ném ngoại lệ khi xử lý {} — bỏ qua trang này, "
                                + "các service khác vẫn chạy",
                        handler.handlerName(), event.url(), e);
            }
        }
    }

    @Override
    public void publishDiscoveredUrl(DiscoveredUrl url) {
        if (url == null) {
            return;
        }
        urlsPublished.incrementAndGet();
        dispatch(urlHandlers, url, "DiscoveredUrl", url.url());
    }

    @Override
    public void publishImage(ImageFound image) {
        if (image == null) {
            return;
        }
        imagesPublished.incrementAndGet();
        dispatch(imageHandlers, image, "ImageFound", image.imageUrl());
    }

    @Override
    public void publishOutlinks(OutlinksExtracted outlinks) {
        if (outlinks == null) {
            return;
        }
        dispatch(outlinkHandlers, outlinks, "OutlinksExtracted", outlinks.sourceUrl());
    }

    /** Gọi từng handler, cô lập lỗi — cùng chính sách với {@link #publishPage}. */
    private <T> void dispatch(List<Consumer<T>> handlers, T payload, String kind, String subject) {
        for (Consumer<T> handler : handlers) {
            try {
                handler.accept(payload);
            } catch (Exception e) {
                publishFailures.incrementAndGet();
                log.warn("Bên nhận {} ném ngoại lệ khi xử lý {} — bỏ qua", kind, subject, e);
            }
        }
    }


     @Override
    public long getPublishFailureCount() {
        return publishFailures.get();
    }

    public long getPagesPublishedCount() {
        return pagesPublished.get();
    }

    public long getUrlsPublishedCount() {
        return urlsPublished.get();
    }

    public long getImagesPublishedCount() {
        return imagesPublished.get();
    }

    /** Số Modular Service đang lắng nghe luồng trang. */
    public int pageHandlerCount() {
        return pageHandlers.size();
    }


}