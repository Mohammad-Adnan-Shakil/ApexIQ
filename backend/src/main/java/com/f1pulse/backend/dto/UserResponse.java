package com.f1pulse.backend.dto;

public class UserResponse {

    private String username;
    private String email;
    private String role;

    public UserResponse(String username, String email, String role) {
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
