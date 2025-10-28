package com.mcq.server.service;

import com.mcq.server.model.User;
import com.mcq.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Generates a token and sets it on the user for password reset.
     * @param email The user's email.
     * @return true if token was set (user found), false otherwise.
     */
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