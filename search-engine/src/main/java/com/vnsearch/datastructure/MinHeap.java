package com.vnsearch.datastructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class MinHeap<T> {

    private final List<T> heap;
    private final Comparator<T> comparator;

    public MinHeap(Comparator<T> comparator) {
        this.comparator = Objects.requireNonNull(comparator, "comparator không được null");
        this.heap = new ArrayList<>();
    }

    public MinHeap(Collection<? extends T> items, Comparator<T> comparator) {
        this.comparator = Objects.requireNonNull(comparator, "comparator không được null");
        this.heap = new ArrayList<>(Objects.requireNonNull(items, "items không được null"));
        heapify();
    }

    private void heapify() {
        for (int i = (heap.size() >>> 1) - 1; i >= 0; i--) {
            siftDown(i);
        }
    }

    public int size() {
        return heap.size();
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    public T peek() {
        if (heap.isEmpty()) {
            throw new NoSuchElementException("Heap rỗng");
        }
        return heap.get(0);
    }

    public void insert(T item) {
        heap.add(item);
        siftUp(heap.size() - 1);
    }

    public T extractMin() {
        if (heap.isEmpty()) {
            throw new NoSuchElementException("Heap rỗng");
        }
        T min = heap.get(0);
        T last = heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) {
            heap.set(0, last);
            siftDown(0);
        }
        return min;
    }

    private void siftUp(int index) {
        T item = heap.get(index);
        while (index > 0) {
            int parent = (index - 1) >>> 1;
            T parentItem = heap.get(parent);
            if (comparator.compare(item, parentItem) >= 0) {
                break; // đã đúng chỗ
            }
            heap.set(index, parentItem);
            index = parent;
        }
        heap.set(index, item);
    }

    private void siftDown(int index) {
        int n = heap.size();
        int half = n >>> 1;
        T item = heap.get(index);
        while (index < half) {
            int child = 2 * index + 1;
            int right = child + 1;
            T childItem = heap.get(child);
            if (right < n) {
                T rightItem = heap.get(right);
                if (comparator.compare(rightItem, childItem) < 0) {
                    child = right;
                    childItem = rightItem;
                }
            }
            if (comparator.compare(childItem, item) >= 0) {
                break;
            }
            heap.set(index, childItem);
            index = child;
        }
        heap.set(index, item);
    }

    /**
     * @param items danh sách nguồn (không bị sửa đổi)
     * @param k     số phần tử cần lấy (nếu k &gt;= items.size() thì trả về
     *              tất cả, đã sắp xếp giảm dần)
     * @param cmp   comparator xác định thứ tự "lớn hơn"
     * @return danh sách k phần tử lớn nhất, sắp xếp giảm dần
     */
    public static <T> List<T> topK(Collection<T> items, int k, Comparator<T> cmp) {
        if (k <= 0 || items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        List<T> seed = new ArrayList<>(Math.min(k, items.size()));
        MinHeap<T> heap = null;
        for (T item : items) {
            if (heap == null) {
                seed.add(item);
                if (seed.size() == k) {
                    heap = new MinHeap<>(seed, cmp);
                }
                continue;
            }
            if (cmp.compare(item, heap.peek()) > 0) {
                heap.extractMin();
                heap.insert(item);
            }
        }
        if (heap == null) {
            heap = new MinHeap<>(seed, cmp);
        }

        List<T> result = new ArrayList<>(heap.size());
        while (!heap.isEmpty()) {
            result.add(heap.extractMin());
        }
        java.util.Collections.reverse(result); 
        return result;
    }

    public static void main(String[] args) {
        MinHeap<Integer> minHeap = new MinHeap<Integer>(Comparator.naturalOrder());
        List<Integer> values = java.util.Arrays.asList(5, 3, 8, 1, 9, 2, 7);
        for (int v : values) {
            minHeap.insert(v);
        }
        System.out.print("Extract theo thứ tự tăng dần: ");
        while (!minHeap.isEmpty()) {
            System.out.print(minHeap.extractMin() + " ");
        }
        System.out.println();

        MinHeap<Integer> built = new MinHeap<>(values, Comparator.naturalOrder());
        System.out.println("Heapify O(n) -> min = " + built.peek());

        List<Integer> top3 = topK(values, 3, Comparator.naturalOrder());
        System.out.println("Top-3 lớn nhất (không sắp xếp toàn bộ): " + top3);
    }
}
