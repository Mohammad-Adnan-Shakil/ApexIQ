package com.f1pulse.backend.dto;

public class UserSummaryResponse {

    private String username;
    private String role;

    public UserSummaryResponse(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public String getUsername() { return username; }
    public String getRole() { return role; }
}
