package com.mcq.server.dto;

import com.mcq.server.model.User;

public class UserDTO {
    private String firstname;
    private String lastname;
    private String email;
    private String username;
    private String role;

    public UserDTO(User user) {
        this.firstname = user.getFirstname();
        this.lastname = user.getLastname();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.role = user.getRole().toString();
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}
