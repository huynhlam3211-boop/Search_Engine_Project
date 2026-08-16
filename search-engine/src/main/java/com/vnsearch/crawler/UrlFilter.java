package com.vnsearch.crawler;

import java.net.URI;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class UrlFilter {

    /**Trong một phiên crawl báo điện tử, ảnh chiếm phần lớn số liên
     * kết bóc được, nên đây là phép lọc tiết kiệm băng thông nhiều nhất. */

    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
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

    public static final Set<String> NON_VI_EN_HOST_PREFIXES = Set.of(
            "cn.", "zh.",           // tieng Trung
            "ja.", "jp.",           // tieng Nhat
            "ko.", "kr.",           // tieng Han
            "ru.",                  // tieng Nga
            "fr.",                  // tieng Phap
            "es.",                  // tieng Tay Ban Nha
            "de.",                  // tieng Duc
            "pt.",                  // tieng Bo Dao Nha
            "ar.",                  // tieng A Rap
            "th.",                  // tieng Thai
            "lo.", "km.");          // tieng Lao, tieng Khmer
    

    private final Set<String> allowedDomains;
    private final Set<String> excludedHostPrefixes;
    private final int maxDepth;
    private final RobotsTxtParser robotsTxtParser;
    private final String userAgent;

    private final AtomicLong rejectedByDepth = new AtomicLong();
    private final AtomicLong rejectedByScheme = new AtomicLong();
    private final AtomicLong rejectedByDomain = new AtomicLong();
    private final AtomicLong rejectedByHostPrefix = new AtomicLong();
    private final AtomicLong rejectedByExtension = new AtomicLong();
    private final AtomicLong rejectedByRobots = new AtomicLong();
    private final AtomicLong accepted = new AtomicLong();

    public UrlFilter(Set<String> allowedDomains, int maxDepth) {
        this(allowedDomains, maxDepth, Set.of(), new RobotsTxtParser(), HtmlDownloader.USER_AGENT);
    }

    public UrlFilter(Set<String> allowedDomains, int maxDepth, Set<String> excludedHostPrefixes) {
        this(allowedDomains, maxDepth, new RobotsTxtParser(), HtmlDowloader.USER_AGENT);
    }

    public UrlFilter(Set<String> allowedDomains, int maxDepth,
                      RobotsTxtParser robotsTxtParser, String userAgent) {
        this(allowedDomains, maxDepth, Set.of(), robotsTxtParser, userAgent);
    }

    
    public UrlFilter(Set<String> allowedDomains, int maxDepth, Set<String> excludedHostPrefixes,
                      RobotsTxtParser robotsTxtParser, String userAgent) {
        
        if (maxDepth < 0){
            throw new IllegalArgumentException("maxDepth phải >= 0, nhận được: " + maxDepth);
        }
        this.allowedDomains = allowedDomains == null ? Set.of() : Set.copyOf(allowedDomains);
        this.excludedHostPrefixes = excludedHostPrefixes == null ? Set.of() : Set.copyOf(excludedHostPrefixes);
        this.maxDepth = maxDepth;
        this.robotsTxtParser = robotsTxtParser;
        this.userAgent = userAgent;

    }

    /**
     * Luật lọc rẻ, <b>không chạm mạng</b>: độ sâu, giao thức, domain, đuôi tệp.
     *
     * @return {@code true} nếu URL xứng đáng được xếp vào hàng đợi
     */

    public boolean accept(String url , int depth) {
        if (depth > maxDepth) {
            rejectedByDepth.incrementAndGet();
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
        if (hasBlockedExtension(uri.getRawPath())) {
            rejectedByExtension.incrementAndGet();
            return false;
        }

        accepted.incrementAndGet();
        return true;
    }

    /**
     * Luật lọc <b>đắt</b>: hỏi {@code robots.txt} của host. Lần đầu gặp một
     * host có thể phải tải qua mạng; các lần sau lấy từ cache của
     * {@link RobotsTxtParser}.
     */

    public boolean isAllowedByRobots(String url){
        boolean allowed = robotsTxtParser.isAllowed(userAgent, url);
        if (!allowed) {
            rejectedByRobots.incrementAndGet();
        }
        return allowed;
    }

    private boolean isAllowedDomain(String host) {
        if (allowedDomains.isEmpty()) {
            return true;
        }
        String lower = host.toLowerCase(Locale.ROOT);
        for (String domain : allowedDomains) {
            String d = domain.toLowerCase(Locale.ROOT);
            if (lower.equals(d) || lower.endsWith("." + d)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Loại các subdomain ngoại ngữ — xem {@link #NON_VI_EN_HOST_PREFIXES}.
     *
     * <p>Khớp theo tiền tố có <b>dấu chấm</b> ({@code "en."} chứ không phải
     * {@code "en"}) để {@code enviro.example.vn} hay {@code endorse.example.vn}
     * không bị loại oan.
     */
    private boolean hasExcludedHostPrefix(String host) {
        if (excludedHostPrefixes.isEmpty()) {
            return false;
        }
        String lower = host.toLowerCase(Locale.ROOT);
        for (String prefix : excludedHostPrefixes) {
            if (lower.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

     /** Xét đuôi tệp của đoạn cuối đường dẫn; đường dẫn không có dấu chấm thì cho qua. */
    private boolean hasBlockedExtension(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        int lastSlash = path.lastIndexOf("/");
        String lastSegment = lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
        int dot = lastSegment.lastIndexOf('.');
        if (dot < 0 || dot == lastSegment.length() - 1) {
            return false;
        }
        String extension = lastSegment.substring(dot + 1).toLowerCase(Locale.ROOT);
        return BLOCKED_EXTENSIONS.contains(extension);
    }

    public long getAcceptedCount() {
        return accepted.get();
    }

    public long getRejectedByDepthCount() {
        return rejectedByDepth.get();
    }

    public long getRejectedBySchemeCount() {
        return rejectedByScheme.get();
    }

    public long getRejectedByDomainCount() {
        return rejectedByDomain.get();
    }

    public long getRejectedByExtensionCount() {
        return rejectedByExtension.get();
    }

    public long getRejectedByRobotsCount() {
        return rejectedByRobots.get();
    }

    public long getTotalRejectedCount() {
        return rejectedByDepth.get() + rejectedByScheme.get() + rejectedByDomain.get()
                + rejectedByExtension.get() + rejectedByRobots.get();
    }


    /* Demo*/

    public static void main(String[] args) {
        UrlFilter filter = new UrlFilter(Set.of("vnexpress.net"), 3);

        System.out.println("Bài viết hợp lệ      : " + filter.accept("https://vnexpress.net/bai-1.html", 1));
        System.out.println("Ngoài domain cho phép: " + filter.accept("https://facebook.com/x", 1));
        System.out.println("Ảnh (đuôi bị chặn)   : " + filter.accept("https://vnexpress.net/anh.jpg", 1));
        System.out.println("Sâu quá maxDepth     : " + filter.accept("https://vnexpress.net/bai-2.html", 9));
        System.out.println("Không phải http(s)   : " + filter.accept("mailto:toasoan@vnexpress.net", 1));

        System.out.println();
        System.out.println("Đã nhận       : " + filter.getAcceptedCount());
        System.out.println("Loại vì domain: " + filter.getRejectedByDomainCount());
        System.out.println("Loại vì đuôi  : " + filter.getRejectedByExtensionCount());
        System.out.println("Loại vì độ sâu: " + filter.getRejectedByDepthCount());
        System.out.println("Loại vì scheme: " + filter.getRejectedBySchemeCount());
    }



}
