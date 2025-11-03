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

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> authenticate(String username, String rawPassword) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(rawPassword, user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }



    public Optional<String> generateResetToken(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            String newToken = UUID.randomUUID().toString();

            LocalDateTime newExpiryDate = LocalDateTime.now().plusHours(24);

            user.setResetPasswordToken(newToken);
            user.setTokenExpiryDate(newExpiryDate);

            userRepository.save(user);

            return Optional.of(newToken);
        }

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

            if (user.getTokenExpiryDate() != null && user.getTokenExpiryDate().isAfter(LocalDateTime.now())) {
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