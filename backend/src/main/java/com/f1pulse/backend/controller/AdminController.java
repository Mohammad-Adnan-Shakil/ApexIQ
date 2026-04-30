package com.f1pulse.backend.controller;

import com.f1pulse.backend.dto.ApiResponse;
import com.f1pulse.backend.dto.UserSummaryResponse;
import com.f1pulse.backend.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administration", description = "Admin endpoints for system management")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    // ✅ Only ADMIN can access
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> getAllUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
) {

    return ResponseEntity.ok(
            new ApiResponse<>(
                    true,
                    "Users fetched successfully",
                    userService.getAllUsers(page, size)
            )
    );
}
}
