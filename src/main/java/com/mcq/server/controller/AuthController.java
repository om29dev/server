package com.mcq.server.controller;

import com.mcq.server.model.User;
import com.mcq.server.service.AuthService;
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
    public ResponseEntity<String> register(@RequestBody User newUser) {
        try {
            authService.registerUser(newUser);
            return new ResponseEntity<>("User registered successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Registration failed: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User loginRequest) {
        return authService.authenticate(loginRequest.getUsername(), loginRequest.getPassword())
                .<ResponseEntity<String>>map(user -> ResponseEntity.ok("Login successful for user: " + user.getUsername()))
                .orElseGet(() -> new ResponseEntity<>("Invalid username or password", HttpStatus.UNAUTHORIZED));
    }
}