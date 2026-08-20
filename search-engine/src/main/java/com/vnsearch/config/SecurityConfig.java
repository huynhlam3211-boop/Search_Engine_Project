package com.vnsearch.config;

import com.vnsearch.auth.SessionStore;
import jakarta.servlet.DispatcherType;
import com.vnsearch.auth.TokenAuthFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; 


/**
 * Phân quyền theo đường dẫn cho toàn bộ REST API.
 * 
 * CONG KHAI                   DA DANG NHAP                 VAI TRO ADMIN
 * ______________________________________________________________________
 * GET /api/search              GET /api/auth/me            POST    /api/admin/crawl
 * GET /api/suggest             POST /api/auth/logout       POST    /api/admin/reindex
 * GET /api/health                                          GET     /api/admin/stats
 * GET /api/images                                          GET     /api/admin/crawl/{id}/status
 * GET /api/feed                                            GET     /api/admin/analytics
 * POST /api/events                                         POST    /api/admin/analytics/reset
 * POST /api/auth/register                                  GET     /api/admin/users
 * POST /api/auth/login                                     POST    /api/admin/users/{ten}/role
 * GET /actuator/health                                     GET     /actuator/**
 * GET /actuator/prometheus                 
 * 
 * 
*/

@Configuration
@EnableWebSecurity
public class SecurityConfig { 
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private static final int MIN_KEY_LENGHT = 16;

    @Value("${app.security.admin-api-key:}")
    private String adminApiKey;

    private String requireAdminApiKey() {
        if (adminApiKey == null || adminApiKey.isBlank()) {
            throw new IllegalStateException(
                    "Thieu app.security.admin-api-key (bien moi truong ADMIN_API_KEY). "
                            + "Cac endpoint /api/admin/** dieu khien crawler va co the tai URL tuy y, "
                            + "nen KHONG duoc phep chay ma khong co khoa. "
                            + "Sinh khoa: openssl rand -hex 32");
        }
        if (adminApiKey.length() < MIN_KEY_LENGTH) {
            throw new IllegalStateException(
                    "app.security.admin-api-key qua ngan (" + adminApiKey.length()
                            + " ky tu, toi thieu " + MIN_KEY_LENGTH + ").");
        }
        return adminApiKey;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SessionStore sessions)
            throws Exception {

    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
        
    }
}