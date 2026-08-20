package com.vnsearch.controller;

import com.vnsearch.auth.Role;
import com.vnsearch.auth.SessionStore;
import com.vnsearch.auth.User;
import com.vnsearch.auth.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List; 

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController { 

    private final UserService users;
    private final SessionStore sessions;

    public AdminUserController(UserService users, SessionStore sessions) {
        this.users = users;
        this.sessions = sessions;
    }

    public record RoleChange(@NotNull(message = "role không được để trống") Role role) {
    }

    @GetMapping
    public List<User.PublicView> list() {
        return users.findAll().stream().map(User::toPublic).toList();
    }

    @PostMapping("/{username}/role")
    public ResponseEntity<User.PublicView> changeRole() {

    }

    @PostMapping("/{username}/disable")
    public ResponseEntity<User.PublicView> disable(@PathVariable String username,
                                                    Authentication authentication) {

    }

    @PostMapping("/{username}/enable")
    public ResponseEntity<User.PublicView> enable(@PathVariable String username)
            throws IOException { 

    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> delete(@PathVariable String username,
                                        Authentication authentication) throws IOException { 

    }
    
}