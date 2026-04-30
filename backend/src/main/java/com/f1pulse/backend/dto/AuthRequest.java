package com.f1pulse.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthRequest {

    @jakarta.validation.constraints.Size(min = 3, max = 50)
    private String username;

    @NotBlank
    private String identifier; // Can be email or username

    @NotBlank
    private String password;

    // getters & setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getEmail() { return identifier; } // For backward compatibility
    public void setEmail(String email) { this.identifier = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
