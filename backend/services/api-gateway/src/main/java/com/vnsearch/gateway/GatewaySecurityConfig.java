package com.vnsearch.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class GatewaySecurityConfig{

    @Bean 
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http,
                                              CorsConfiguration corsSource) {
        return http.csrf(ServerHttpSecurity.CsrfSpec.disable)
                   .cors(cors -> cors.configurationSource(corsSource))
                   .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                   .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                   .authorizeExchange(exchange -> exchange
                            .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                            .pathMatchers("/oath2/**","/.well-known/**").permitAll()
                            .pathMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login","/api/auth/logout","/api/auth/refresh").permitAll()
                            .pathMatchers(HttpMethod.GET,"/api/search","/api/suggest","/api/images","/api/feed","/api/health").permitAll()
                            .pathMatchers("/ws/**").permitAll()
                            .pathMatchers(HttpMethod.POST,"/api/event").permitAll()
                            .pathMatchers(HttpMethod.GET,"/api/football/**").permitAll()
                            .pathMatchers("/actuator/health/**","/actuator/prometheus").permitAll()
                            .pathMatchers("/swagger-ui/**","/swagger-ui.html","/v3/api-docs/**").permitAll()
                            .pathMatchers("/api/admin/**","/actuator/**").hasRole("ADMIN")
                            .anyExchange().authenticated()
                                )
                   .oauth2ResourceServer(oauth -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(
                                                                        new ReactiveJwtAuthenticationConverterAdapter(new JwtRoleConverter()))))
                   .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)
                   ))
                   .build();                                        
    
    }

    @Bean 
    public CorsConfigurationSource CorsConfigurationSource(
            @Value("${app.cors.allowed-origin:http://localhost:5173}") String allowedOrigin) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins.split("\\s*,\\s")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTION"));
        config.setAllowedHeaders(List.of("Accept", "Authorization", "Content-Type",
                "X-API-Key", "X-Idempotency-Key", "X-Device-Id", "If-Match"));
        // Giao diện đọc hai header này để biết còn bao nhiêu lượt gọi.
        config.setExposedHeaders(List.of("X-RateLimit-Remaining", "Retry-After"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    static final class JwtRoleConverter implements Converter<Jwt, AbstractAuthenticationToken> {

        @Override
        public AbstractAuthenticationToken convert(Jwt jwt) {
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null) {
                for (String role : roles) {
                    if (role != null && !role.isBlank()) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.trim()));
                    }
                }
            }
            return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
        }
    }

}