package com.mcq.server.dto;

public class LoginRequest {
    private String username; // <-- FIXED (was Username)
    private String password; // <-- FIXED (was Password)

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}