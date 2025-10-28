package com.mcq.server.controller;

import com.mcq.server.dto.LoginRequest;
import com.mcq.server.model.User;
import com.mcq.server.service.AuthService;
import com.mcq.server.dto.MessagewithUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
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
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {

        // Authenticate the user using the UserService
        // Replace with your actual authentication call
        Optional<User> authenticatedUser = authService.authenticate(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        );

        if (authenticatedUser.isPresent()) {
            // --- Implement Cookie Logic using Spring's ResponseCookie ---

            // In a real application, you would generate a secure, short-lived JWT or Session ID here.
            String sessionToken = authenticatedUser.get().getUuid().toString();

            // 1. Create and configure the secure ResponseCookie 🍪
            ResponseCookie springCookie = ResponseCookie.from("AUTH_TOKEN", sessionToken)
                    .httpOnly(true)            // RECOMMENDED: Prevents client-side JavaScript access
                    .secure(false)             // Set to 'true' in production with HTTPS
                    .path("/")                 // Make the cookie available to all paths
                    .maxAge(7 * 24 * 60 * 60)  // Set expiry to 7 days (in seconds)
                    .sameSite("Lax")           // RECOMMENDED: Helps mitigate CSRF attacks (e.g., "Strict", "Lax", or "None")
                    .build();

            // 2. Build the ResponseEntity with the cookie in the Headers
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, springCookie.toString()) // Add the cookie header
                    .body("Login successful. Authentication cookie set.");

        } else {
            // Return an unauthorized response
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password.");
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