package com.vnsearch.crawler.frontier;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class UrlFrontierTest {
    private static UrlFrontier strictFrontier() {
        return strictFrontier(UrlFrontier.DEFAULT_MAX_SIZE);
    }

    private static UrlFrontier strictFrontier(int maxSize) {
        return new UrlFrontier(maxSize, new DefaultPrioritizer(), new StrictPrioritySelector(),
                UrlFrontier.DEFAULT_BACK_QUEUE_COUNT);
    }

    @Test
    void emptyFrontierReturnsNull() {

    }

    @Test
    void rejectsDuplicateUrls() {

    }

    @Test
    void vnDomainGetsHigherPriority() {

    }

    @Test
    void shallowerDepthGetsHigherPriority() {

    }

    @Test
    void moreBacklinksGetsHigherPriority() {

    }

    @Test
    void moreBacklinksGetsHigherPriority() {

    }

    @Test
    void sameLevelKeepsDiscoveryOrder() {

    }

    @Test
    void politenessDelayForcesRoundRobinAcrossDomains() {

    }

    @Test
    void respectsMaxSizeCap() {

    }

    @Test
    void rejectsInvalidConstructorArguments() {

    }
    
    @Test
    void tracksDistinctDomainCount() {

    }

    @Test
    void domainCountShrinksWhenUrlsAreHandedOut() {

    }

    @Test
    void urlsMoveFromFrontTierToBackTier() {

    }

    @Test
    void neverHandsOutSameUrlTwiceUnderConcurrency() throws InterruptedException {
        
    }
}
