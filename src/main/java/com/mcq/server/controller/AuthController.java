package com.mcq.server.controller;

import com.mcq.server.model.User;
import com.mcq.server.service.AuthService;
import com.mcq.server.dto.MessagewithUUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<MessagewithUUID> register(@RequestBody User newUser) {
        try {
            User user = authService.registerUser(newUser);
            return new ResponseEntity<>(new MessagewithUUID("Registration Successful.", user.getUuid()), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new MessagewithUUID("Registration failed." + e.getMessage()),HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<MessagewithUUID> login(@RequestBody User loginRequest) {
        // Attempt to authenticate the user
        Optional<User> authenticatedUserOptional = authService.authenticate(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        );

        // Check if the Optional contains a user (authentication successful)
        if (authenticatedUserOptional.isPresent()) {
            User user = authenticatedUserOptional.get();
            // Return the UUID with an HTTP 200 OK status
            return new ResponseEntity<>(new MessagewithUUID("Login Successful.", user.getUuid()), HttpStatus.OK);
        } else {
            // Authentication failed, return an appropriate error response
            // HttpStatus.UNAUTHORIZED (401) is the standard for failed authentication
            return new ResponseEntity<>(new MessagewithUUID("Login failed."), HttpStatus.UNAUTHORIZED);
            // You could also return a more detailed error message if needed,
            // e.g., throw a custom exception or return a specific error body.
        }
    }

    // Logout is implemented in Security Config

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null) {
            return ResponseEntity.badRequest().body("Email is required.");
        }

        Optional<String> tokenOptional = authService.generateResetToken(email);

        if (tokenOptional.isPresent()) {
            // WARNING: This returns the token directly and is INSECURE for production.
            return ResponseEntity.ok(
                    Map.of(
                            "message", "Token generated successfully",
                            "resetToken", tokenOptional.get()
                    )
            );
        } else {
            return ResponseEntity.status(404).body("User with that email not found.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        if (token == null || newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body("Token and newPassword are required.");
        }

        boolean success = authService.resetPassword(token, newPassword);

        if (success) {
            return ResponseEntity.ok("Password has been successfully reset.");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired token.");
        }
    }
}