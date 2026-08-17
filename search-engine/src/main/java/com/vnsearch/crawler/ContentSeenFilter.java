package com.vnsearch.crawler;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ContentSeenFilter {
    
    private final Set<String> fingerprints = ConcurrentHashMap.newKeySet();

    private final AtomicLong duplicates = new AtomicLong();
    private final AtomicLong blankSkipped = new AtomicLong();

    public boolean seenBefore(String bodyText) {

    }

    /** Vân tay SHA-256 (dạng hex) của văn bản sau khi chuẩn hoá. */
    public static String fingerprint(String text) {

    }

    private static String normalize(String text) {

    }

    public int size() {

    }

    public long getDuplicateCount() {

    }

    public long getBlankSkippedCount() {

    }

    public static void main(String[] args) {
        ContentSeenFilter filter = new ContentSeenFilter();

        String goc = "Đội tuyển Việt Nam thắng 2-0 trong trận đấu tối qua.";
        String banSao = "Đội tuyển   Việt Nam thắng 2-0\ntrong trận đấu tối qua."; // chỉ khác khoảng trắng
        String khac = "Giá vàng trong nước tăng phiên thứ ba liên tiếp.";

        System.out.println("Trang gốc đã thấy chưa?  " + filter.seenBefore(goc));
        System.out.println("Bản sao đã thấy chưa?    " + filter.seenBefore(banSao) + "  <- bị phát hiện trùng");
        System.out.println("Bài khác đã thấy chưa?   " + filter.seenBefore(khac));
        System.out.println("Số nội dung phân biệt    : " + filter.size());
        System.out.println("Số trang trùng bị vứt    : " + filter.getDuplicateCount());
    }
}