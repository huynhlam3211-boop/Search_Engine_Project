package com.vnsearch.datastructure;

import java.nio.charset.StandardCharsets;

public class BloomFilter {

    private final long[] bits;
    private final int numBits;
    private final int numHashes;

    public BloomFilter(int expectedItems, double falsePositiveRate) {
        if (expectedItems <= 0) {
            throw new IllegalArgumentException("expectedItems phải > 0");
        }
        if (falsePositiveRate <= 0 || falsePositiveRate >= 1) {
            throw new IllegalArgumentException("falsePositiveRate phải trong khoảng (0, 1)");
        }
        double ln2 = Math.log(2);
        int m = (int) Math.ceil(-expectedItems * Math.log(falsePositiveRate) / (ln2 * ln2));
        m = Math.max(m, 64);
        int k = (int) Math.round((double) m / expectedItems * ln2);
        this.numBits = m;
        this.numHashes = Math.max(k, 1);
        this.bits = new long[(m + 63) / 64];
    }

    BloomFilter(int numBits, int numHashes, boolean rawConfig) {
        this.numBits = numBits;
        this.numHashes = numHashes;
        this.bits = new long[(numBits + 63) / 64];
    }

    public int getNumBits() {
        return numBits;
    }

    public int getNumHashes() {
        return numHashes;
    }

    public void add(String item) {
        long h1 = hash1(item);
        long h2 = hash2(item);
        for (int i = 0; i < numHashes; i++) {
            int idx = indexFor(h1, h2, i);
            setBit(idx);
        }
    }

    public boolean mightContain(String item) {
        long h1 = hash1(item);
        long h2 = hash2(item);
        for (int i = 0; i < numHashes; i++) {
            int idx = indexFor(h1, h2, i);
            if (!getBit(idx)) {
                return false;
            }
        }
        return true;
    }

    long[] baseHashes(String item) {
        return new long[] { hash1(item), hash2(item) };
    }

    int[] indicesFor(String item) {
        long h1 = hash1(item);
        long h2 = hash2(item);
        int[] out = new int[numHashes];
        for (int i = 0; i < numHashes; i++) {
            out[i] = indexFor(h1, h2, i);
        }
        return out;
    }

    public int countSetBits() {
        int count = 0;
        for (long word : bits) {
            count += Long.bitCount(word);
        }
        return count;
    }

    public double currentFalsePositiveRate() {
        double q = (double) countSetBits() / numBits;
        return Math.pow(q, numHashes);
    }

    private int indexFor(long h1, long h2, int i) {
        long combined = h1 + (long) i * h2;
        return (int) Math.floorMod(combined, (long) numBits);
    }

    private void setBit(int index) {
        bits[index / 64] |= (1L << (index % 64));
    }

    private boolean getBit(int index) {
        return (bits[index / 64] & (1L << (index % 64))) != 0;
    }

    private static long hash1(String s) {
        byte[] data = s.getBytes(StandardCharsets.UTF_8);
        long hash = 0xcbf29ce484222325L;
        for (byte b : data) {
            hash ^= (b & 0xffL);
            hash *= 0x100000001b3L;
        }
        return hash;
    }

    private static long hash2(String s) {
        long hash = 1125899906842597L;
        for (int i = 0; i < s.length(); i++) {
            hash = 31 * hash + s.charAt(i);
        }
        hash ^= (hash >>> 33);
        hash *= 0xff51afd7ed558ccdL;
        hash ^= (hash >>> 33);
        return hash;
    }

    public static void main(String[] args) {
        BloomFilter filter = new BloomFilter(1000, 0.01);
        System.out.println("numBits=" + filter.getNumBits() + " numHashes=" + filter.getNumHashes());

        filter.add("https://vnexpress.net/");
        filter.add("https://tuoitre.vn/");

        System.out.println("mightContain(vnexpress) = " + filter.mightContain("https://vnexpress.net/"));
        System.out.println("mightContain(tuoitre)   = " + filter.mightContain("https://tuoitre.vn/"));
        System.out.println("mightContain(chưa thêm) = " + filter.mightContain("https://khong-ton-tai.vn/"));
    }
}