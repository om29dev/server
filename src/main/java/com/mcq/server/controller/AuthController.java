package com.mcq.server.controller;

import com.mcq.server.model.User;
import com.mcq.server.service.AuthService;
import com.mcq.server.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
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

    // Logout is implemented in Security Config

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null) {
            return ResponseEntity.badRequest().body("Email is required.");
        }

        userService.generateResetToken(email);

        // Return a generic message for security.
        return ResponseEntity.ok("If an account with that email exists, a password reset process has been initiated.");
    }

    /**
     * Endpoint 2: Resets the password using the token.
     * POST /api/users/reset-password
     * Expected Request Body: { "token": "uuid-token-string", "newPassword": "new-secure-password" }
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        if (token == null || newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body("Token and newPassword are required.");
        }

        boolean success = userService.resetPassword(token, newPassword);

        if (success) {
            return ResponseEntity.ok("Password has been successfully reset.");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired token.");
        }
    }
}