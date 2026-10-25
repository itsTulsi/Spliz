package com.fairshare.controller;

import com.fairshare.dto.*;
import com.fairshare.entity.UserEntity;
import com.fairshare.security.UserContext;
import com.fairshare.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe() {
        UserEntity currentUser = UserContext.requireCurrentUser();
        return ResponseEntity.ok(authService.getMe(currentUser));
    }

    @PutMapping("/settings")
    public ResponseEntity<UserDto> updateSettings(@RequestBody UpdateSettingsRequest request) {
        UserEntity currentUser = UserContext.requireCurrentUser();
        UserDto updated = authService.updateSettings(currentUser, request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authService.logout(authHeader.substring(7).trim());
        }
        return ResponseEntity.noContent().build();
    }
}
