package com.vnsearch.auth;

import java.time.Instant;

public record User(
        String username,
        String passwordHash,
        Role role,
        boolean enabled,
        Instant createdAt,
        Instant lastLoginAt) { }