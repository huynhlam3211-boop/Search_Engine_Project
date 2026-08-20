package com.vnsearch.crawler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SsrfProtectionTest {

    @Nested
    @DisplayName("Phép kiểm tra địa chỉ")
    class AddressChecks {

        @Test
        @DisplayName("Chặn đủ các dải địa chỉ nội bộ")
        void blocksPrivateRanges() {
            assertAll(
                    () -> assertTrue(blocked("127.0.0.1"), "loopback IPv4"),
                    () -> assertTrue(blocked("::1"), "loopback IPv6"),
                    () -> assertTrue(blocked("10.0.0.1"), "10/8"),
                    () -> assertTrue(blocked("172.16.5.4"), "172.16/12"),
                    () -> assertTrue(blocked("192.168.1.1"), "192.168/16"),
                    () -> assertTrue(blocked("169.254.169.254"), "link-local / metadata"),
                    () -> assertTrue(blocked("0.0.0.0"), "any-local"),
                    () -> assertTrue(blocked("fc00::1"), "ULA IPv6"),
                    () -> assertTrue(blocked("100.64.0.1"), "CGNAT (RFC 6598)"),
                    () -> assertTrue(blocked("::ffff:127.0.0.1"), "IPv4 trong vỏ IPv6"));
        }

        @Test
        @DisplayName("Cho qua địa chỉ công khai")
        void allowsPublicAddresses() {
            assertAll(
                    () -> assertFalse(blocked("8.8.8.8")),
                    () -> assertFalse(blocked("1.1.1.1")),
                    () -> assertFalse(blocked("2001:4860:4860::8888")));
        }

        private boolean blocked(String literal) throws Exception {
            return SeedUrlValidator.isBlockedAddress(InetAddress.getByName(literal));
        }

        @Test
        @DisplayName("Chặn tên máy nội bộ theo TÊN, không cần phân giải")
        void blocksInternalHostnames() {
            assertAll(
                    () -> assertTrue(SeedUrlValidator.isBlockedHostname("localhost")),
                    () -> assertTrue(SeedUrlValidator.isBlockedHostname("LOCALHOST")),
                    () -> assertTrue(SeedUrlValidator.isBlockedHostname("app.localhost")),
                    () -> assertTrue(SeedUrlValidator.isBlockedHostname("metadata.google.internal")),
                    () -> assertTrue(SeedUrlValidator.isBlockedHostname(null)),
                    () -> assertFalse(SeedUrlValidator.isBlockedHostname("vnexpress.net")));
        }
    }

    @Nested
    @DisplayName("SeedUrlValidator — tầng controller")
    class SeedValidation {

        @Test
        @DisplayName("Từ chối scheme không phải http/https")
        void rejectsNonHttpSchemes() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> SeedUrlValidator.validate("file:///etc/passwd")),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> SeedUrlValidator.validate("ftp://example.com/")),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> SeedUrlValidator.validate("")));
        }

        @Test
        @DisplayName("Thông báo lỗi KHÔNG lộ địa chỉ nội bộ")
        void errorMessageLeaksNothing() {
            IllegalArgumentException blocked = assertThrows(IllegalArgumentException.class,
                    () -> SeedUrlValidator.validate("http://localhost:8080/"));
            IllegalArgumentException unresolvable = assertThrows(IllegalArgumentException.class,
                    () -> SeedUrlValidator.validate(
                            "http://ten-mien-chac-chan-khong-ton-tai-9c1f2a.invalid/"));

            assertAll(
                    () -> assertFalse(blocked.getMessage().contains("127.0.0.1"),
                            "Thông báo không được chứa địa chỉ IP"),
                    () -> assertFalse(blocked.getMessage().contains("localhost"),
                            "Thông báo không được chứa tên máy"),
                    () -> assertEquals(blocked.getMessage(), unresolvable.getMessage(),
                            "Bị chặn và không phân giải được phải cho cùng một thông báo"));
        }
    }

    @Nested
    @DisplayName("HtmlDownloader — tầng tải trang")
    class DownloaderGuard {

        private final HtmlDownloader downloader = new HtmlDownloader();

        @Test
        @DisplayName("Chặn URL trỏ vào mạng nội bộ, dù không qua controller")
        void blocksInternalTargets() {
            assertAll(
                    () -> assertThrows(HtmlDownloader.BlockedTargetException.class,
                            () -> downloader.download("http://127.0.0.1/admin")),
                    () -> assertThrows(HtmlDownloader.BlockedTargetException.class,
                            () -> downloader.download("http://169.254.169.254/latest/meta-data/")),
                    () -> assertThrows(HtmlDownloader.BlockedTargetException.class,
                            () -> downloader.download("http://192.168.1.1/")),
                    () -> assertThrows(HtmlDownloader.BlockedTargetException.class,
                            () -> downloader.download("http://localhost:8080/")));
        }

        @Test
        @DisplayName("Chặn scheme không phải http/https")
        void blocksNonHttpSchemes() {
            assertAll(
                    () -> assertThrows(HtmlDownloader.BlockedTargetException.class,
                            () -> downloader.download("file:///etc/passwd")),
                    () -> assertThrows(HtmlDownloader.BlockedTargetException.class,
                            () -> downloader.download("jar:file:///tmp/x.jar!/y")));
        }

        @Test
        @DisplayName("URL bị chặn KHÔNG được thử lại")
        void doesNotRetryBlockedTargets() {
            long before = downloader.getRetryCount();
            assertThrows(IOException.class, () -> downloader.download("http://10.0.0.1/"));
            assertEquals(before, downloader.getRetryCount(),
                    "Không được thử lại một URL đã bị chặn vì lý do bảo mật");
        }
    }
}
