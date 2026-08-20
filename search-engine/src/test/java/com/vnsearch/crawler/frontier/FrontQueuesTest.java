package com.vnsearch.crawler.frontier;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class FrontQueuesTest {
    
    private static CrawlTask task(String url) {
        return new CrawlTask(url, "a.com", 0);
    }

    @Test
    void stricSelectorAlwaysTakesHighestLevelFirst() {

    }

    @Test
    void sameLevelIsFifo() {

    }

    @Test
    void tracksSizePerLevel() {

    }

    @Test
    void rejectsInvalidArgument() {

    }

    @Test 
    void weightedSelectoreNeverStarvesTheLowestLevel() {

    }

    @Test
    void weightedSelectorStillFavoursHighLevels() {

    }

    @Test 
    void weightedSelectorIsReproducibleWithTheSameSeed(){
        
    }

    private static int levelOf(String url) {
        String tail = url.substring(url.lastIndexOf('/')+1);
        return Interget.parseInt(tail.substring(0, tail.indexOf('-')));
    }
}
