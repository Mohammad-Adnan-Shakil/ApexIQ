package com.f1pulse.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class FavoriteDriverRequest {

    @NotBlank(message = "Favorite driver is required")
    @Size(max = 10, message = "Driver code must be 10 characters or less")
    @Pattern(regexp = "^[A-Z]{2,4}$", message = "Driver code must be 2-4 uppercase letters")
    private String favoriteDriver;

    public FavoriteDriverRequest() {}

    public FavoriteDriverRequest(String favoriteDriver) {
        this.favoriteDriver = favoriteDriver;
    }

    public String getFavoriteDriver() {
        return favoriteDriver;
    }

    public void setFavoriteDriver(String favoriteDriver) {
        this.favoriteDriver = favoriteDriver;
    }
}
