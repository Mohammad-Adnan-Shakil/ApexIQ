package com.f1pulse.backend.controller;

import com.f1pulse.backend.dto.AuthRequest;
import com.f1pulse.backend.model.User;
import com.f1pulse.backend.repository.UserRepository;
import com.f1pulse.backend.security.JWTUtil;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository,
                          JWTUtil jwtUtil,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    // ✅ REGISTER
    @PostMapping("/register")
    public String register(@Valid @RequestBody AuthRequest request) {

        Optional<User> existing = userRepository.findByUsername(request.getUsername());

        if (existing.isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");

        userRepository.save(user);

        return "User registered";
    }

    // ✅ LOGIN
    @PostMapping("/login")
    public String login(@Valid @RequestBody AuthRequest request) {

        Optional<User> existing = userRepository.findByUsername(request.getUsername());

        if (existing.isPresent() &&
            passwordEncoder.matches(request.getPassword(), existing.get().getPassword())) {

            return jwtUtil.generateToken(request.getUsername());
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }
}