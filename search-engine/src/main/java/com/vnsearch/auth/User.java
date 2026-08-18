package com.vnsearch.auth;

import java.time.Instant;

/**
 * 
 * @param username     định danh, chữ thường, duy nhất
 * @param passwordHash BCrypt, đã kèm salt bên trong chuỗi
 * @param enabled      {@code false} = khoá tài khoản mà không xoá dữ liệu
 * @param lastLoginAt  {@code null} khi chưa đăng nhập lần nào
 * 
 */

public record User(
        String username,
        String passwordHash,
        Role role,
        boolean enabled,
        Instant createdAt,
        Instant lastLoginAt) { 

    public record PublicView( 
                String username,
                Role role,
                boolean enabled,
                Instant createdAt,
                Instant lastLoginAt) {
    }

    public PublicView toPublic() {
        return new PublicView(username, role, enabled, createdAt, lastLoginAt);
    }

    public User withRole(Role newRole) {
        return new User(username, passwordHash, newRole, enabled, createdAt, lastLoginAt);
    }

    public User withEnabled(boolean nowEnabled) {
        return new User(username, passwordHash, role, nowEnabled, createdAt, lastLoginAt);
    }

    public User withLastLoginAt(Instant at) {
        return new User(username, passwordHash, role, enabled, createdAt, at);
    }

    public User withPasswordHash(String newHash) {
        return new User(username, newHash, role, enabled, createdAt, lastLoginAt);
    }
}