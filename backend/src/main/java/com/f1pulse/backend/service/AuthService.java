package com.deltabox.backend.service;

import com.deltabox.backend.dto.AuthRequest;
import com.deltabox.backend.dto.AuthResponse;
import com.deltabox.backend.dto.GoogleAuthRequest;

public interface AuthService {

    AuthResponse register(AuthRequest request);

    AuthResponse login(AuthRequest request);

    AuthResponse googleAuth(GoogleAuthRequest request);
}
