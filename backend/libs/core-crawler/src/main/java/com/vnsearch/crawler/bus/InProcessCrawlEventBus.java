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

    public InProcessCrawlEventBus subscribePages(PageEventHandler handler) {
        if (handler != null) {
            pageHandlers.add(handler);
        }
        return this;
    }

    public InProcessCrawlEventBus subscribeDiscoveredUrls(Consumer<DiscoveredUrl> handler) {
        if (handler != null) {
            urlHandlers.add(handler);
        }
        return this;
    }

    public InProcessCrawlEventBus subscribeOutlinks(Consumer<OutlinksExtracted> handler) {
        if (handler != null) {
            outlinkHandlers.add(handler);
        }
        return this;
    }

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
                log.warn("Modular service {} threw an exception while handling {} — skipping this page, "
                                + "other services keep running",
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

    private <T> void dispatch(List<Consumer<T>> handlers, T payload, String kind, String subject) {
        for (Consumer<T> handler : handlers) {
            try {
                handler.accept(payload);
            } catch (Exception e) {
                publishFailures.incrementAndGet();
                log.warn("Subscriber {} threw an exception while handling {} — skipping", kind, subject, e);
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

    public int pageHandlerCount() {
        return pageHandlers.size();
    }


}