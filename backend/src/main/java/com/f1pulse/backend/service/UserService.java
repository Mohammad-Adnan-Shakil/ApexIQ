package com.f1pulse.backend.service;

import com.f1pulse.backend.dto.UserResponse;
import com.f1pulse.backend.dto.UserSummaryResponse;

import java.util.List;

public interface UserService {

    UserResponse getCurrentUser(String email);

    List<UserSummaryResponse> getAllUsers(int page, int size);
}
