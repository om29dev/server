package com.mcq.server.controller;

import com.mcq.server.dto.UserDTO;
import com.mcq.server.model.User;
import com.mcq.server.repository.UserRepository;
import com.mcq.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping(path = "/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @GetMapping
    public @ResponseBody Iterable<UserDTO> getAllUsers() {
        Iterable<User> users = userRepository.findAll();
        return StreamSupport.stream(users.spliterator(), false)
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping(path = "/{uuid}")
    public ResponseEntity<User> getUserById(@PathVariable UUID uuid) {
        Optional<User> userData = userRepository.findById(uuid);
        return userData.map(user -> new ResponseEntity<>(user, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

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