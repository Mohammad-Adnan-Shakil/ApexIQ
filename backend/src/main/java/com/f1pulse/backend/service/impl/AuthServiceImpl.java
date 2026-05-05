package com.f1pulse.backend.service.impl;

import com.f1pulse.backend.dto.AuthRequest;
import com.f1pulse.backend.dto.AuthResponse;
import com.f1pulse.backend.exception.UserAlreadyExistsException;
import com.f1pulse.backend.model.User;
import com.f1pulse.backend.repository.UserRepository;
import com.f1pulse.backend.security.JwtService;
import com.f1pulse.backend.service.AuthService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "security.enabled", havingValue = "true", matchIfMissing = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    // ✅ REGISTER
    @Override
    public AuthResponse register(AuthRequest request) {
        logger.info("Registration attempt for email: {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("User already exists with email: {}", request.getEmail());
            throw new UserAlreadyExistsException("User already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername() != null ? request.getUsername() : request.getEmail());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");

        userRepository.save(user);
        logger.info("User registered successfully: {}", user.getEmail());

        // ✅ Convert to Spring Security UserDetails
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles("USER")
                .build();

        String token = jwtService.generateToken(userDetails);
        logger.info("JWT token generated for new user: {}", user.getEmail());

        return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getRole());
    }

    // ✅ LOGIN
    @Override
    public AuthResponse login(AuthRequest request) {
        logger.info("Login attempt for identifier: {}", request.getIdentifier());

        try {
            // Determine if identifier is email or username
            String identifier = request.getIdentifier();
            User user;

            if (identifier.contains("@")) {
                // Treat as email
                logger.info("Looking up user by email: {}", identifier);
                user = userRepository.findByEmail(identifier)
                        .orElseThrow(() -> {
                            logger.error("User not found with email: {}", identifier);
                            return new RuntimeException("Invalid credentials");
                        });
            } else {
                // Treat as username
                logger.info("Looking up user by username: {}", identifier);
                user = userRepository.findByUsername(identifier)
                        .orElseThrow(() -> {
                            logger.error("User not found with username: {}", identifier);
                            return new RuntimeException("Invalid credentials");
                        });
            }

            logger.info("User found: {} with role: {}", user.getEmail(), user.getRole());

            // Authenticate using the user's email (UserDetailsService uses email)
            logger.info("Attempting authentication for user: {}", user.getEmail());
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getEmail(),
                            request.getPassword()
                    )
            );
            logger.info("Authentication successful for user: {}", user.getEmail());

            // ✅ Convert to Spring Security UserDetails
            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .roles(user.getRole())
                    .build();

            String token = jwtService.generateToken(userDetails);
            logger.info("JWT token generated successfully for user: {}", user.getEmail());

            return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getRole());
        } catch (Exception e) {
            logger.error("Login failed for identifier: {}", request.getIdentifier(), e);
            throw e;
        }
    }
}
