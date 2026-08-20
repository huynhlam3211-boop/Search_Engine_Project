package com.vnsearch.controller;

import com.vnsearch.auth.Role;
import com.vnsearch.auth.SessionStore;
import com.vnsearch.auth.User;
import com.vnsearch.auth.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController { 
    private final UserService users;
    private final SessionStore sessions;

    public AuthController(UserService users, SessionStore sessions) {
        this.users = users;
        this.sessions = sessions;
    }

    public record Credentials(
            @NotBlank(message = "Tên tài khoản không được để trống")
            @Size(max = 32, message = "Tên tài khoản tối đa 32 ký tự")
            String username,

            @NotBlank(message = "Mật khẩu không được để trống")
            @Size(max = 200, message = "Mật khẩu tối đa 200 ký tự")
            String password) {
    }

    public record LoginResponse(String token, Instant expiresAt, User.PublicView user) {
    }

    @PostMapping("/register")
    public ResponseEntity<User.PublicView> register(@Valid @RequestBody Credentials request)
            throws IOException { 

    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody Credentials request) { 

    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) { 

    }

    private static String bearerToken(HttpServletRequest request) { 

    }

    public record PasswordChange() {

    }

    @PostMapping("/password")
    public ResponseEntity<Map<String, Object>> changePassword() {

    }

    @PostMapping("/logout-all")
    public Map<String, Object> logoutEverywhere(Authentication authentication) {

    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(Authentication authentication) { 
        
    }
}