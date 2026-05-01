package com.f1pulse.backend.controller;

import com.f1pulse.backend.dto.ApiResponse;
import com.f1pulse.backend.dto.UserSummaryResponse;
import com.f1pulse.backend.repository.DriverRepository;
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
    private final DriverRepository driverRepository;

    public AdminController(UserService userService, DriverRepository driverRepository) {
        this.userService = userService;
        this.driverRepository = driverRepository;
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

    // ✅ Only ADMIN can access
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/cleanup-duplicates")
    public ResponseEntity<?> cleanupDuplicates() {
        try {
            long beforeCount = driverRepository.count();
            driverRepository.deleteDuplicates();
            long afterCount = driverRepository.count();
            
            long removed = beforeCount - afterCount;
            
            return ResponseEntity.ok(String.format(
                "Cleanup completed. Removed %d duplicate drivers. %d drivers remain.", 
                removed, afterCount
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to cleanup duplicates: " + e.getMessage());
        }
    }
}
