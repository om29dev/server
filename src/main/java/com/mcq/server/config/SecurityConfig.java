package com.mcq.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // <-- IMPORT THIS
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration; // <-- IMPORT THIS
import org.springframework.web.cors.CorsConfigurationSource; // <-- IMPORT THIS
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // <-- IMPORT THIS

import java.util.Arrays; // <-- IMPORT THIS

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // 1. Password Encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Security Filter Chain (UPDATED)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Apply the CORS configuration first
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Disable CSRF as you are using a stateless auth (sessions)
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // (KEY FIX 1) Explicitly allow all preflight OPTIONS requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // (KEY FIX 2) Explicitly allow your auth endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // Secure classroom APIs
                        .requestMatchers("/api/classrooms/**").authenticated()

                        // Permit any other request (you can tighten this later)
                        .anyRequest().permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(200);
                            response.getWriter().write("Logged out successfully");
                            response.getWriter().flush();
                        })
                );
        return http.build();
    }

    // 3. (NEW) CORS Configuration Bean
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // This MUST match the URL your Vite client is running on (default is 5173)
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://127.0.0.1:5173"));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Origin", "Accept"));

        // This is ESSENTIAL for your client to send and receive session cookies
        configuration.setAllowCredentials(true);

        configuration.setMaxAge(3600L); // Cache preflight response for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}