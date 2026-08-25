package com.vnsearch.crawler.frontier;

public final class StrictPrioritySelector implements FrontQueueSelector {

    @Override
    public int select(int[] queueSizes) {
        for (int i = 0; i < queueSizes.length; i++) {
            if (queueSizes[i] > 0) {
                return i;
            }
        }
        return -1;
    }
}