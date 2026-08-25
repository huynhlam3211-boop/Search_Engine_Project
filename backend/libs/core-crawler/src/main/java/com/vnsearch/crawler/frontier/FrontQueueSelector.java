package com.vnsearch.crawler.frontier;

@FunctionalInterface
public interface FrontQueueSelector {
    int select(int[] queueSizes);
}    