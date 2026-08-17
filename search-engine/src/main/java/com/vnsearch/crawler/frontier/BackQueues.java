package com.vnsearch.crawler.frontier;

import com.vnsearch.datastructure.MinHeap;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BackQueues {
    private final List<Deque<CrawlTask>> queues;
    private final String[] boundHost;
    private final long[] availableAt;
    private final Map<String, Integer> hostToQueue = new HashMap<>();

    private final MinHeap<Integer> ready;
    private final boolean[] inReady;

    private final Deque<Integer> freeSlots = new ArrayDeque();
    private final boolean[] empty;

    private final long politenessDelayMs;
    private int pending;

    public BackQueues(int queueCount, long politenessDelayMs) {

    }

    public void refillFrom(FrontQueues front) {

    }

    public CrawlTask poll(long now) {

    }

    public long earliesAvailableAt() {

    }

    public int pendingCount() {

    }

    public int boundHostCount() {

    }

    public int queueCount() {

    }

    public String hostOfQueue(int slot) {

    }


    private int nextFreeSlot() {

    }

    public boolean fillSlot(int slot, FrontQueues front) {

    }

    private void bind(int slot, String host) {

    }

    private void push(int slot, CrawlTask task) {

    }

    private void markEmpty(int slot) {
        
    }
}