package com.mcq.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. Password Encoder (Crucial for security)
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt is the standard for secure password storage
        return new BCryptPasswordEncoder();
    }

    // 2. Security Filter Chain (Handles Logout)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity in API
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Allow all requests for now
                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")  // The URL to trigger logout
                        .invalidateHttpSession(true)    // Invalidate the session
                        .deleteCookies("JSESSIONID")    // Delete the session cookie
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(200);
                            response.getWriter().write("Logged out successfully");
                            response.getWriter().flush();
                        })
                );
        return http.build();
    }
}