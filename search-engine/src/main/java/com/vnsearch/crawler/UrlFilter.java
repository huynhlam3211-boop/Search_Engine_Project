package com.vnsearch.crawler;

import java.net.URI;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class UrlFilter {


    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
        "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico", "tif", "tiff",
        "css", "js", "json", "xml", "rss", "atom", "woff", "woff2", "ttf", "eot",
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "odt", "csv",
        "zip", "rar", "7z", "tar", "gz", "bz2", "exe", "msi", "apk", "dmg", "iso",
        "mp3", "mp4", "avi", "mkv", "mov", "wmv", "flv", "wav", "m4a", "webm");

    public static final Set<String> NON_VI_EN_HOST_PREFIXES = Set.of(
            "cn.", "zh.",
            "ja.", "jp.",
            "ko.", "kr.",
            "ru.",
            "fr.",
            "es.",
            "de.",
            "pt.",
            "ar.",
            "th.",
            "lo.", "km.");
    

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
            throw new IllegalArgumentException("maxDepth must be >= 0, got: " + maxDepth);
        }
        this.allowedDomains = allowedDomains == null ? Set.of() : Set.copyOf(allowedDomains);
        this.excludedHostPrefixes = excludedHostPrefixes == null ? Set.of() : Set.copyOf(excludedHostPrefixes);
        this.maxDepth = maxDepth;
        this.robotsTxtParser = robotsTxtParser;
        this.userAgent = userAgent;

    }


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



    public static void main(String[] args) {
        UrlFilter filter = new UrlFilter(Set.of("vnexpress.net"), 3);

        System.out.println("Valid article        : " + filter.accept("https://vnexpress.net/article-1.html", 1));
        System.out.println("Outside allowed domain: " + filter.accept("https://facebook.com/x", 1));
        System.out.println("Image (blocked ext)  : " + filter.accept("https://vnexpress.net/photo.jpg", 1));
        System.out.println("Deeper than maxDepth : " + filter.accept("https://vnexpress.net/article-2.html", 9));
        System.out.println("Not http(s)          : " + filter.accept("mailto:toasoan@vnexpress.net", 1));

        System.out.println();
        System.out.println("Accepted           : " + filter.getAcceptedCount());
        System.out.println("Rejected by domain : " + filter.getRejectedByDomainCount());
        System.out.println("Rejected by ext    : " + filter.getRejectedByExtensionCount());
        System.out.println("Rejected by depth  : " + filter.getRejectedByDepthCount());
        System.out.println("Rejected by scheme : " + filter.getRejectedBySchemeCount());
    }



}
