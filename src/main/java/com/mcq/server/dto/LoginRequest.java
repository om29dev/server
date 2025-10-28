package com.mcq.server.dto;

public class LoginRequest {
    private String Username;
    private String Password;

    public LoginRequest(String username, String password) {
        this.Username = username;
        this.Password = password;
    }

    public String getUsername() {
        return Username;
    }

    public String getPassword() {
        return Password;
    }
}
