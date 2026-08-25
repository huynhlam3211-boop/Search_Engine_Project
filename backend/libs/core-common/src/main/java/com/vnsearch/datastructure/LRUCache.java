package com.vnsearch.datastructure;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LRUCache<K, V> { 

    private static class Node<K,V>{
        K key;
        V value;
        Node<K,V> prev;
        Node<K,V> next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private final int capacity;
    private final Map<K, Node<K,V>> map;
    private final Node<K,V> head;
    private final Node<K,V> tail;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public LRUCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity phải > 0");
        }
        this.capacity = capacity;
        this.map = new HashMap<>();
        this.head = new Node<>(null, null);
        this.tail = new Node<>(null, null);
        head.next = tail;
        tail.prev = head;
    }

    /** O(1) — trả về giá trị và đánh dấu là vừa được dùng (MRU). */
    public V get(K key) {
        lock.writeLock().lock();
        try {
            Node<K, V> node = map.get(key);
            if (node == null) {
                return null;
            }
            moveToFront(node);
            return node.value;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /** O(1) — thêm/cập nhật giá trị, đẩy node ở LRU-tail ra nếu vượt capacity. */
    public void put(K key, V value) {
        lock.writeLock().lock();
        try {
            Node<K, V> existing = map.get(key);
            if (existing != null) {
                existing.value = value;
                moveToFront(existing);
                return;
            }
            Node<K, V> node = new Node<>(key, value);
            map.put(key, node);
            addToFront(node);
            if (map.size() > capacity) {
                Node<K, V> lru = tail.prev;
                removeNode(lru);
                map.remove(lru.key);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int size() {
        lock.readLock().lock();
        try {
            return map.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean containsKey(K key) {
        lock.readLock().lock();
        try {
            return map.containsKey(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    private void addToFront(Node<K, V> node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }

    private void removeNode(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void moveToFront(Node<K, V> node) {
        removeNode(node);
        addToFront(node);
    }

    /** Demo minh hoạ nhỏ để chụp màn hình làm báo cáo. */
    public static void main(String[] args) {
        LRUCache<String, String> cache = new LRUCache<>(2);
        cache.put("q=máy tính", "kết quả A");
        cache.put("q=trình duyệt", "kết quả B");
        System.out.println("get(máy tính) = " + cache.get("q=máy tính")); // MRU hoá lại "máy tính"
        cache.put("q=bloom filter", "kết quả C"); // đẩy "trình duyệt" ra vì là LRU
        System.out.println("get(trình duyệt) sau khi bị đẩy = " + cache.get("q=trình duyệt")); // null
        System.out.println("get(máy tính) vẫn còn = " + cache.get("q=máy tính"));
        System.out.println("get(bloom filter) = " + cache.get("q=bloom filter"));
    }
}