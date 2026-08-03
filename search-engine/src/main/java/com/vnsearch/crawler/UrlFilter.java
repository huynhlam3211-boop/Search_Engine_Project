package com.vnsearch.crawler;

import java.net.URI;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class UrlFilter {

    private static final Set<String> BLOCK_EXTENSIONS = Set.of(
        // ảnh
        "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico", "tif", "tiff",
        // tài nguyên tĩnh của trang
        "css", "js", "json", "xml", "rss", "atom", "woff", "woff2", "ttf", "eot",
        // tài liệu
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "odt", "csv",
        // nén và cài đặt
        "zip", "rar", "7z", "tar", "gz", "bz2", "exe", "msi", "apk", "dmg", "iso",
        // đa phương 
        "mp3", "mp4", "avi", "mkv", "mov", "wmv", "flv", "wav", "m4a", "webm");

    public UrlFilter(Set<String> allowedDomains, int maxDepth) {

    }

    public UrlFilter(Set<String> allowedDomains, int maxDepth,
                    RobotsTxtParset robotsTxtParser, String userAgent) {
        
        if (maxDepth < 0){
            throw new IllegalArgumentException("maxDepth phải >= 0, nhận được: " + maxDepth);
        }
        this.allowedDomains = allowedDomains == null ? Set.of() : Set.copyOf(allowedDomains);
        this.maxDepth = maxDepth;
        this.robotsTxtParser = robotsTxtParser;
        this.userAgent = userAgent;

    }

    public boolean accept(String url , int depth) {
        if (depth > maxDepth) {
            rejectByDepth.incrementAndGet();
            return false;
        }
        if (url == null || url.isBlank()) {
            rejectedByScheme.incrementAndGet();
            return false;
        }

        URI uri;
        try {
            uri = URI.create(url);
        } catch (Exception e) {
            rejectedByScheme.incrementAndGet();
            return false;
        }

        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            rejectedByScheme.incrementAndGet();
            return false;
        }

        String host = uri.getHost();
        if (host == null) {
            rejectedByScheme.incrementAndGet();
            return false;
        }

        if (!isAllowedDomain(host)) {
            rejectedByDomain.incrementAndGet();
            return false;
        }
        if (hashBlockedExtension(uri.getRawPath())) {
            rejectedByExtension.incrementAndGet();
            return false;
        }

        accepted.incrementAndGet();
        return true;
    }
    


    public boolean accept(String url, int depth) {
        
    }
}
