package com.f1pulse.backend.controller;

import com.f1pulse.backend.model.User;
import com.f1pulse.backend.repository.UserRepository;
import com.f1pulse.backend.security.JWTUtil;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private JWTUtil JWTUtil;

    public AuthController(UserRepository userRepository, JWTUtil jwtUtil) {
        this.userRepository = userRepository;
        this.JWTUtil = jwtUtil;
        this.jwtUtil = new JWTUtil();
    }

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        userRepository.save(user);
        return "User registered";
    }

    @PostMapping("/login")
    public String login(@RequestBody User user) {

        User existing = userRepository.findByUsername(user.getUsername());

        if (existing != null && existing.getPassword().equals(user.getPassword())) {
            return jwtUtil.generateToken(user.getUsername());
        }

        throw new RuntimeException("Invalid credentials");
    }
}