package com.f1pulse.backend.controller;

import com.f1pulse.backend.dto.UserResponse;
import com.f1pulse.backend.dto.FavoriteDriverRequest;
import com.f1pulse.backend.service.UserService;
import com.f1pulse.backend.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User Profile", description = "User profile management and preferences")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String email = auth.getName();

    UserResponse user = userService.getCurrentUser(email);

    return ResponseEntity.ok(
            new ApiResponse<>(true, "User fetched successfully", user)
    );
}

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        UserResponse user = userService.getCurrentUser(email);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Profile fetched successfully", user)
        );
    }

    @PutMapping("/profile/favorite-driver")
    public ResponseEntity<ApiResponse<UserResponse>> updateFavoriteDriver(
            @Valid @RequestBody FavoriteDriverRequest request) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        UserResponse user = userService.updateFavoriteDriver(email, request);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Favorite driver updated successfully", user)
        );
    }
}
