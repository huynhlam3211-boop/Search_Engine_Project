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
        if (queueCount <= 0) {
            throw new IllegalArgumentException("queueCount phải > 0, nhận được: " + queueCount);
        }
        if (politenessDelayMs < 0) {
            throw new IllegalArgumentException(
                    "politenessDelayMs phải >= 0, nhận được: " + politenessDelayMs);
        }
        this.queues = new ArrayList<>(queueCount);
        for (int i = 0; i < queueCount; i++) {
            queues.add(new ArrayDeque<>());
        }
        this.boundHost = new String[queueCount];
        this.availableAt = new long[queueCount];
        this.inReady = new boolean[queueCount];
        this.empty = new boolean[queueCount];
        this.politenessDelayMs = politenessDelayMs;
        this.ready = new MinHeap<>((a, b) -> {
            int byTime = Long.compare(availableAt[a], availableAt[b]);
            return byTime != 0 ? byTime : Integer.compare(a, b);
        });

        for (int i = 0; i < queueCount; i++) {
            empty[i] = true;
            freeSlots.addLast(i);
        }
    }

    public void refillFrom(FrontQueues front) {
        while (!front.isEmpty()) {
            int slot = nextFreeSlot();
            if (slot < 0) {
                return;
            }
            if (!fillSlot(slot, front)) {
                return;
            }
        }
    }

    public CrawlTask poll(long now) {
        if (ready.isEmpty()) {
            return null;
        }
        int slot = ready.peek();
        if (availableAt[slot] > now) {
            return null; 
        }

        ready.extractMin();
        inReady[slot] = false; 

        CrawlTask task = queues.get(slot).pollFirst();
        pending--;
        availableAt[slot] = now + politenessDelayMs;

        if (queues.get(slot).isEmpty()) {
            markEmpty(slot);
        } else {
            ready.insert(slot);
            inReady[slot] = true;
        }
        return task;
    }

    public long earliestAvailableAt() {
        return ready.isEmpty() ? Long.MAX_VALUE : availableAt[ready.peek()];
    }

    public int pendingCount() {
        return pending;
    }

    public int boundHostCount() {
        return hostToQueue.size();
    }

    public int queueCount() {
        return queues.size();
    }

    public String hostOfQueue(int slot) {
        return boundHost[slot];
    }


    private int nextFreeSlot() {
        while (!freeSlots.isEmpty()) {
            int slot = freeSlots.peekFirst();
            if (empty[slot]) {
                return slot;
            }
            freeSlots.pollFirst(); // mục cũ
        }
        return -1;
    }

    private boolean fillSlot(int slot, FrontQueues front) {
        while (true) {
            CrawlTask task = front.poll();
            if (task == null) {
                return false;
            }
            Integer owner = hostToQueue.get(task.host());
            if (owner != null && owner != slot) {
                push(owner, task); // host này đã có chủ, trả về đúng hàng đợi của nó
                continue;
            }
            bind(slot, task.host());
            push(slot, task);
            return true;
        }
    }

    private void bind(int slot, String host) {
        String previous = boundHost[slot];
        if (previous != null && !previous.equals(host)) {
            hostToQueue.remove(previous); // giữ Mapping Table không phình quá số hàng đợi
        }
        boundHost[slot] = host;
        hostToQueue.put(host, slot);
    }

    private void push(int slot, CrawlTask task) {
        queues.get(slot).addLast(task);
        pending++;
        empty[slot] = false;
        if (!inReady[slot]) {
            ready.insert(slot);
            inReady[slot] = true;
        }
    }

    private void markEmpty(int slot) {
        if (!empty[slot]) {
            empty[slot] = true;
            freeSlots.addLast(slot);
        }
    }
}