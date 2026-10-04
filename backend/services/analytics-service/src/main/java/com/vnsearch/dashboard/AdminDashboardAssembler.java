package com.vnsearch.dashboard;

import com.vnsearch.analytics.AdminDashboard;
import com.vnsearch.analytics.CorpusStats;
import com.vnsearch.analytics.UsageAnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;
import java.util.function.Supplier;

@Service
public class AdminDashboardAssembler {

    private static final Logger log = LoggerFactory.getLogger(AdminDashboardAssembler.class);

    /** Giá trị trả về khi không lấy được khối chỉ mục. */
    private static final AdminDashboard.IndexStats INDEX_UNKNOWN =
            new AdminDashboard.IndexStats(0, 0, 0L, 0.0, "khong-ro", 0L);

    /** Giá trị trả về khi không lấy được khối tài khoản. */
    private static final AdminDashboard.AccountStats ACCOUNTS_UNKNOWN =
            new AdminDashboard.AccountStats(0, 0, 0, 0);

    private final UsageAnalyticsService analytics;
    private final RestClient crawlerClient;
    private final RestClient authClient;

    public AdminDashboardAssembler(
            UsageAnalyticsService analytics,
            RestClient.Builder builder,
            @Value("${app.clients.crawler-service.url:http://crawler-service:8083}") String crawlerUrl,
            @Value("${app.clients.auth-service.url:http://auth-service:8081}") String authUrl) {
        this.analytics = analytics;
        this.crawlerClient = builder.clone().baseUrl(crawlerUrl).build();
        this.authClient = builder.clone().baseUrl(authUrl).build();
    }

    public AdminDashboard assemble(int top, String callerAuthHeader) {
        return new AdminDashboard(
                Instant.now(),
                analytics.snapshot(top),
                safeFetch("corpus", null, () -> corpusStats(callerAuthHeader)),
                safeFetch("chỉ mục", INDEX_UNKNOWN,
                        () -> indexStats(callerAuthHeader)),
                safeFetch("tài khoản", ACCOUNTS_UNKNOWN,
                        () -> accountStats(callerAuthHeader)));
    }

    private <T> T safeFetch(String name, T fallback, Supplier<T> source) {
        try {
            return source.get();
        } catch (Exception e) {
            log.warn("Không lấy được khối '{}' cho bảng điều khiển: {}", name, e.toString());
            return fallback;
        }
    }

    private AdminDashboard.IndexStats indexStats(String authHeader) {
        Map<String, Object> body = crawlerClient.get()
                .uri("/api/admin/stats")
                .headers(headers -> forwardIdentity(headers, authHeader))
                .retrieve()
                .body(Map.class);
        if (body == null) {
            return INDEX_UNKNOWN;
        }
        return new AdminDashboard.IndexStats(
                toInt(body.get("totalDocuments")),
                toInt(body.get("totalTerms")),
                toLong(body.get("indexSizeBytes")),
                toDouble(body.get("cacheHitRate")),
                body.get("scorer") == null ? "khong-ro" : body.get("scorer").toString(),
                toLong(body.get("bloomFilterBits")));
    }

    private CorpusStats corpusStats(String authHeader) {
        return crawlerClient.get()
                .uri("/api/admin/corpus-stats")
                .headers(headers -> forwardIdentity(headers, authHeader))
                .retrieve()
                .body(CorpusStats.class);
    }

    private AdminDashboard.AccountStats accountStats(String authHeader) {
        Map<String, Object> body = authClient.get()
                .uri("/api/admin/users/stats")
                .headers(headers -> forwardIdentity(headers, authHeader))
                .retrieve()
                .body(Map.class);
        if (body == null) {
            return ACCOUNTS_UNKNOWN;
        }
        return new AdminDashboard.AccountStats(
                toInt(body.get("total")),
                toInt(body.get("admins")),
                toInt(body.get("disabled")),
                toInt(body.get("activeSessions")));
    }

    private static void forwardIdentity(HttpHeaders headers, String authHeader) {
        if (authHeader != null && !authHeader.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authHeader);
        }
    }

    private static int toInt(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private static long toLong(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }

    private static double toDouble(Object value) {
        return value instanceof Number number ? number.doubleValue() : 0.0;
    }
}
