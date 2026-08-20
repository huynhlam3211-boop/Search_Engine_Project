package com.vnsearch.crawler.bus;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InProcessCrawlEventBusTest { 
    private static PageEvent page(String url) {
        return new PageEvent(url, "a.com", 0, "t", "than bai", "vi",
                "<html></html>", "hash", Instant.EPOCH, "job-1");
    }

    @Test
    void everyHandlerReceivesEveryPage() {

    }

    @Test
    void oneFailingHandlerDoesNotStopTheOthers() {

    }

    @Test
    void publishNeverThrowsToTheCaller() {

    }

    @Test
    void allFourChannelsAreDelivered() {

    }

    @Test
    void nullPayloadsAreIgnored() { 

    }

    @Test
    void nullSubscribersAreIgnored() {

    }

    @Test
    void isThreadSafeUnderConcurrentPublishing() throws Exception {

    }

    @Test
    void noopBusSwallowsEverything() {
        
    }
}