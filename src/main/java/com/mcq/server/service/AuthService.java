package com.mcq.server.service;

import com.mcq.server.model.User;
import com.mcq.server.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Handles user registration, including password hashing
    public User registerUser(User user) {
        // Hash the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // Basic logic for login
    public Optional<User> authenticate(String username, String rawPassword) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Check if the raw password matches the stored hashed password
            if (passwordEncoder.matches(rawPassword, user.getPassword())) {
                return userOptional;
            }
        }
        return Optional.empty();
    }

// In your UserService or AuthService

    public Optional<String> generateResetToken(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // 1. Generate a brand new, unique token.
            // This implicitly "resets" the old token, replacing it in the database.
            String newToken = UUID.randomUUID().toString();

            // 2. Define a new expiry date (Token is valid for 24 hours).
            LocalDateTime newExpiryDate = LocalDateTime.now().plusHours(24);

            // 3. Update the user entity with the new token and expiry.
            user.setResetPasswordToken(newToken);
            user.setTokenExpiryDate(newExpiryDate);

            // 4. Save the updated user object to persist the changes.
            userRepository.save(user);

            // 5. Return the new token.
            return Optional.of(newToken);
        }

        // Return empty if user is not found.
        return Optional.empty();
    }
    /**
     * Resets the user's password using a token.
     * @param token The reset token.
     * @param newPassword The new password (will be hashed).
     * @return true if password was successfully reset, false otherwise.
     */
    public boolean resetPassword(String token, String newPassword) {
        Optional<User> userOptional = userRepository.findByResetPasswordToken(token);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Check if the token is valid (not expired)
            if (user.getTokenExpiryDate() != null && user.getTokenExpiryDate().isAfter(LocalDateTime.now())) {
                // HASH the new password before saving
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetPasswordToken(null);
                user.setTokenExpiryDate(null);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }
}