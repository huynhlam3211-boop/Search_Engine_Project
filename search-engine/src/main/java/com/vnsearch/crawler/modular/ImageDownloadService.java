package com.vnsearch.crawler.modular;

import com.vnsearch.crawler.DnsResolver;
import com.vnsearch.crawler.HtmlDownloader;
import com.vnsearch.crawler.SeedUrlValidator;
import com.vnsearch.crawler.UrlCanonicalizer;
import com.vnsearch.crawler.bus.CrawlEventBus;
import com.vnsearch.crawler.bus.ImageFound;
import com.vnsearch.crawler.bus.PageEvent;
import com.vnsearch.crawler.bus.PageEventHandler;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class ImageDownloadService implements PageEventHandler {

    private static final Logger log = LoggerFactory.getLogger(ImageDownloadService.class);

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".avif", ".bmp", ".svg");

    public static final int DEFAULT_MAX_IMAGES_PER_PAGE = 50;
    public static final long DEFAULT_MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    public static final int DEFAULT_TIMEOUT_MS = 8_000;

    private final CrawlEventBus bus;
    private final DnsResolver dnsResolver;
    private final boolean downloadEnabled;
    private final int maxImagesPerPage;
    private final long maxImageBytes;
    private final int timeoutMs;

    private final AtomicLong pagesProcessed = new AtomicLong();
    private final AtomicLong imagesFound = new AtomicLong();
    private final AtomicLong imagesSkippedByExtension = new AtomicLong();
    private final AtomicLong imagesOverPageLimit = new AtomicLong();
    private final AtomicLong imagesMissingAlt = new AtomicLong();
    private final AtomicLong imagesDownloaded = new AtomicLong();
    private final AtomicLong imagesBlocked = new AtomicLong();
    private final AtomicLong downloadFailures = new AtomicLong();
    private final AtomicLong bytesDownloaded = new AtomicLong();

    public ImageDownloadService(CrawlEventBus bus) {
        this(bus, new DnsResolver(), false, DEFAULT_MAX_IMAGES_PER_PAGE,
                DEFAULT_MAX_IMAGE_BYTES, DEFAULT_TIMEOUT_MS);
    }

    public ImageDownloadService(CrawlEventBus bus, DnsResolver dnsResolver,
                                 boolean downloadEnabled, int maxImagesPerPage,
                                 long maxImageBytes, int timeoutMs) {
        if (bus == null) {
            throw new IllegalArgumentException("ImageDownloadService cần một bus");
        }
        if (maxImagesPerPage <= 0) {
            throw new IllegalArgumentException(
                    "maxImagesPerPage phải > 0, nhận được: " + maxImagesPerPage);
        }
        if (maxImageBytes <= 0) {
            throw new IllegalArgumentException(
                    "maxImageBytes phải > 0, nhận được: " + maxImageBytes);
        }
        this.bus = bus;
        this.dnsResolver = dnsResolver == null ? new DnsResolver() : dnsResolver;
        this.downloadEnabled = downloadEnabled;
        this.maxImagesPerPage = maxImagesPerPage;
        this.maxImageBytes = maxImageBytes;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public void onPage(PageEvent event) {
        if (event.html() == null || event.html().isBlank()) {
            return;
        }
        pagesProcessed.incrementAndGet();

        Document document = Jsoup.parse(event.html(), event.url());

        Set<String> seen = new LinkedHashSet<>();
        int emitted = 0;

        for (Element img : document.select("img")) {
            String raw = resolveSource(img);
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String imageUrl = UrlCanonicalizer.canonicalize(raw);
            if (imageUrl == null || imageUrl.isBlank() || !seen.add(imageUrl)) {
                continue;
            }
            if (!hasImageExtension(imageUrl)) {
                imagesSkippedByExtension.incrementAndGet();
                continue;
            }
            if (emitted >= maxImagesPerPage) {
                imagesOverPageLimit.incrementAndGet();
                continue;
            }

            String alt = img.attr("alt");
            ImageFound found = describe(event, imageUrl, alt,
                    parseDimension(img.attr("width")), parseDimension(img.attr("height")));

            imagesFound.incrementAndGet();
            if (found.missingAlt()) {
                imagesMissingAlt.incrementAndGet();
            }
            bus.publishImage(found);
            emitted++;
        }
    }

    private static String resolveSource(Element img) {
        for (String attr : new String[] {"data-src", "data-original", "src"}) {
            if (!img.hasAttr(attr)) {
                continue;
            }
            String resolved = img.absUrl(attr);
            if (resolved != null && !resolved.isBlank()) {
                return resolved;
            }
        }
        return null;
    }

    private ImageFound describe(PageEvent event, String imageUrl, String alt,
                                 int width, int height) {
        ImageFound metadata = ImageFound.metadataOnly(
                event.url(), event.host(), imageUrl, alt, width, height);
        if (!downloadEnabled) {
            return metadata;
        }
        try {
            byte[] body = fetchImage(imageUrl);
            imagesDownloaded.incrementAndGet();
            bytesDownloaded.addAndGet(body.length);
            return new ImageFound(event.url(), event.host(), imageUrl, alt, width, height,
                    body.length, sha256Hex(body));
        } catch (BlockedImageException e) {
            imagesBlocked.incrementAndGet();
            log.warn("Chan tai anh {} tu trang {}: {}", imageUrl, event.url(), e.getMessage());
            return metadata;
        } catch (Exception e) {
            downloadFailures.incrementAndGet();
            log.debug("Khong tai duoc anh {}: {}", imageUrl, e.toString());
            return metadata;
        }
    }

    private byte[] fetchImage(String imageUrl) throws Exception {
        assertTargetAllowed(imageUrl);

        Connection.Response response = Jsoup.connect(imageUrl)
                .userAgent(HtmlDownloader.USER_AGENT)
                .timeout(timeoutMs)
                .ignoreContentType(true)
                .maxBodySize((int) Math.min(maxImageBytes, Integer.MAX_VALUE))
                .followRedirects(false)
                .execute();

        int status = response.statusCode();
        if (status >= 300) {
            throw new java.io.IOException("HTTP " + status + " khi tai anh " + imageUrl);
        }
        return response.bodyAsBytes();
    }

    private void assertTargetAllowed(String url) throws BlockedImageException {
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            throw new BlockedImageException("URL anh khong hop le");
        }
        String scheme = uri.getScheme();
        if (scheme == null
                || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new BlockedImageException("Chi chap nhan http/https");
        }
        String host = uri.getHost();
        if (SeedUrlValidator.isBlockedHostname(host)) {
            throw new BlockedImageException("Ten may nam trong danh sach chan");
        }
        InetAddress address;
        try {
            address = dnsResolver.resolve(host);
        } catch (Exception e) {
            throw new BlockedImageException("Khong phan giai duoc ten may");
        }
        if (SeedUrlValidator.isBlockedAddress(address)) {
            throw new BlockedImageException("Dia chi noi bo, khong duoc phep tai");
        }
    }

    private static final class BlockedImageException extends Exception {
        BlockedImageException(String message) {
            super(message);
        }
    }

    static boolean hasImageExtension(String url) {
        String path = url.toLowerCase(Locale.ROOT);
        int query = path.indexOf('?');
        if (query >= 0) {
            path = path.substring(0, query);
        }
        int fragment = path.indexOf('#');
        if (fragment >= 0) {
            path = path.substring(0, fragment);
        }
        for (String ext : IMAGE_EXTENSIONS) {
            if (path.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private static int parseDimension(String value) {
        if (value == null || value.isBlank()) {
            return -1;
        }
        try {
            // Cắt đuôi đơn vị: HTML cho phép width="800px".
            String digits = value.trim().replaceAll("[^0-9].*$", "");
            return digits.isEmpty() ? -1 : Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static String sha256Hex(byte[] data) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(data);
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(Character.forDigit((b >> 4) & 0xf, 16));
                hex.append(Character.forDigit(b & 0xf, 16));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("JVM khong ho tro SHA-256", e);
        }
    }

    @Override
    public String handlerName() {
        return "Image Download";
    }

    public boolean isDownloadEnabled() {
        return downloadEnabled;
    }

    public long getPagesProcessedCount() {
        return pagesProcessed.get();
    }

    public long getImagesFoundCount() {
        return imagesFound.get();
    }

    public long getImagesMissingAltCount() {
        return imagesMissingAlt.get();
    }

    public long getImagesSkippedByExtensionCount() {
        return imagesSkippedByExtension.get();
    }

    public long getImagesOverPageLimitCount() {
        return imagesOverPageLimit.get();
    }

    public long getImagesDownloadedCount() {
        return imagesDownloaded.get();
    }

    public long getImagesBlockedCount() {
        return imagesBlocked.get();
    }

    public long getDownloadFailureCount() {
        return downloadFailures.get();
    }

    public long getBytesDownloadedCount() {
        return bytesDownloaded.get();
    }

    public double getMissingAltRate() {
        long total = imagesFound.get();
        return total == 0 ? 0.0 : (double) imagesMissingAlt.get() / total;
    }

    public double getAverageImagesPerPage() {
        long pages = pagesProcessed.get();
        return pages == 0 ? 0.0 : (double) imagesFound.get() / pages;
    }
}
