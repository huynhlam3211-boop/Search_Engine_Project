package com.vnsearch.crawler.bus;

@FunctionalInterface
public interface PageEventHandler {
    void onPage(PageEvent event);

    default String handlerName() {
        return getClass().getSimpleName();
    }
}