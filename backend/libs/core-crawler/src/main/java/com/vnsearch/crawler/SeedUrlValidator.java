package com.vnsearch.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Set;

public final class SeedUrlValidator{
    private static final Logger log = LoggerFactory.getLogger(SeedUrlValidator.class);

    private static final Set<String> BLOCKED_HOSTNAMES = Set.of(
            "localhost","metadata","metadata.google.internal",
            "instance-data","169.254.169.254"
    );

    static final String REJECTED = "Seed URL khong duoc phep crawl. Kiem tra lai dia chi";

    private SeedUrlValidator() {

    }

    /**
     * Nem ngoai le neu URL khong an toan de crawl.
     *
     * @throws IllegalArgumentException kem ly do doc duoc, de tang REST tra ve
     *                                  400 thay vi 500
     */

    public static void validate(String rawUrl) {
        if(rawUrl == null || rawUrl.isBlank()){
            throw new IllegalArgumentException("Seed URL rong");
        }

        URI uri;
        try {
            uri = URI.create(rawUrl.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Seed URL ko hop lej: " + rawUrl);
        }

        String scheme = uri.getScheme();
        if (scheme == null
                || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException(
                    "Chi chap nhan http/https, nhan duoc: " + rawUrl);
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("Seed URL khong co ten may: " + rawUrl);
        }

        if (isBlockedHostname(host)) {
            log.warn("Chặn seed URL: tên máy nằm trong danh sách chặn ({})", host);
            throw new IllegalArgumentException(REJECTED);
        }

        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            log.warn("Chặn seed URL: không phân giải được tên máy ({})", host);
            throw new IllegalArgumentException(REJECTED);
        }

        for (InetAddress address : addresses) {
            if (isBlockedAddress(address)) {
                log.warn("Chặn seed URL trỏ tới địa chỉ nội bộ: {} -> {}",
                        host, address.getHostAddress());
                throw new IllegalArgumentException(REJECTED);
            }
        }
    }

    public static boolean isBlockedHostname(String host) {
        if (host == null || host.isBlank()) {
            return true;
        }
        String lowerHost = host.toLowerCase(Locale.ROOT);
        return BLOCKED_HOSTNAMES.contains(lowerHost) || lowerHost.endsWith(".localhost");
    }

    public static boolean isBlockedAddress(InetAddress address) {
        return address.isLoopbackAddress()      // 127.0.0.0/8, ::1
                || address.isLinkLocalAddress() // 169.254.0.0/16 (metadata dam may), fe80::/10
                || address.isSiteLocalAddress() // 10/8, 172.16/12, 192.168/16
                || address.isAnyLocalAddress()  // 0.0.0.0, ::
                || address.isMulticastAddress()
                || isUniqueLocalIpv6(address)   // fc00::/7
                || isCarrierGradeNat(address);  // 100.64.0.0/10
    }

    /** {@code fc00::/7} — dai dia chi rieng cua IPv6, khong co san phep kiem tra. */
    private static boolean isUniqueLocalIpv6(InetAddress address) {
        byte[] bytes = address.getAddress();
        return bytes.length == 16 && (bytes[0] & 0xFE) == 0xFC;
    }

    /**
     * {@code 100.64.0.0/10} — dai dung chung cua nha mang (RFC 6598).
     * {@link InetAddress#isSiteLocalAddress()} khong tinh dai nay la noi bo,
     * nhung trong mot mang dam may no van tro toi ha tang khong cong khai.
     */
    private static boolean isCarrierGradeNat(InetAddress address) {
        byte[] bytes = address.getAddress();
        return bytes.length == 4
                && (bytes[0] & 0xFF) == 100
                && (bytes[1] & 0xFF) >= 64
                && (bytes[1] & 0xFF) <= 127;
    }

    
}