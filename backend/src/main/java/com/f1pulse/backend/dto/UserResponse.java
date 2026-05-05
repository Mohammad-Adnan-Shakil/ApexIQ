package com.f1pulse.backend.dto;

public class UserResponse {

    private String username;
    private String email;
    private String role;
    private String favoriteDriver;

    public UserResponse(String username, String email, String role, String favoriteDriver) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.favoriteDriver = favoriteDriver;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getFavoriteDriver() { return favoriteDriver; }
}
