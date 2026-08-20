package com.vnsearch.crawler.frontier;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultPrioritizerTest {
    private final DefaultPrioritizer prioritizer = new DefaultPrioritizer();

    @Test
    void rejectsNonPositiveLevels() {

    }

    @Test
    void depthIsTheStartingPoint() { 

    }

    @Test
    void vnDomainMovesUpOneLevel() {

    }

    @Test
    void manyBacklinksMoveUpOneLevel() {

    }

    @Test
    void bothSignalsStackButAreClampedAtZero() {

    }

    @Test
    void deepUrlsAreClampedToTheLowestLevel() {

    }

    @Test
    void sideSignalsCannotOverturnDepthByMoreThanTwoLevels() {

    }

    @Test
    void everyLevelIsWithinRange() {
        
    }
}