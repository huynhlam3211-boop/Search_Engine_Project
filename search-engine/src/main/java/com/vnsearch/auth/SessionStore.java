package com.vnsearch.auth;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SessionStore { 
    
    public static final int SESSION_HOURS = 12;

    public static final int MAX_SESSIONS = 10000;

    private static final int TOKEN_BYTES = 32;

    public record Session(String username, Role role, Instant createdAt, Instant expiresAt) {

    }

    private final SecureRandom = new SecureRandom();
    
}