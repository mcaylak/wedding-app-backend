package com.wedding.photo.controller;

import com.wedding.photo.dto.AuthRequest;
import com.wedding.photo.dto.AuthResponse;
import com.wedding.photo.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/verify-key")
    public ResponseEntity<AuthResponse> verifyKey(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.verifyKey(request);
        return ResponseEntity.ok(response);
    }
}