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

    public boolean generateResetToken(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String token = UUID.randomUUID().toString();
            // Token is valid for 24 hours
            LocalDateTime expiryDate = LocalDateTime.now().plusHours(24);

            user.setResetPasswordToken(token);
            user.setTokenExpiryDate(expiryDate);
            userRepository.save(user);

            // In a real application, you would now use a separate email service
            // to send an email with a link containing this 'token'.
            return true;
        }
        return false;
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