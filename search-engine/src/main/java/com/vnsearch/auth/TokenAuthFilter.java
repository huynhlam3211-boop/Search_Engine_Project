package com.vnsearch.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

public class TokenAuthFilter extends OncePerRequestFilter { 
    public static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer";

    private final SessionStore sessions;

    public TokenAuthFilter(SessionStore sessions) {
        this.sessions = sessions;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

    }

    private static Optional<String> extractToken(HttpServletRequest request {

    })
}