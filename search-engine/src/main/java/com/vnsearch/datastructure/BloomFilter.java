package com.vnsearch.datastructure;

import java.nio.charset.StandardCharsets;

public class BloomFilter {

    

    // Demo
    public static void main(String[] args) {
        BloomFilter filter = new BloomFilter(1000, 0.01);
        System.out.println("numBits" + filter.getNumBits()+ "numHashes" + filter.getNumHashes());

        filter.add("https://vnexpress.net/");
        filter.add("https://tuoitre.vn/");

        System.out.println("mightContain(vnexpress) = " + filter.mightContain("https://vnexpress.net/"));
        System.out.println("mightContain(tuoitre)   = " + filter.mightContain("https://tuoitre.vn/"));
        System.out.println("mightContain(chưa thêm) = " + filter.mightContain("https://khong-ton-tai.vn/"));
    }
}