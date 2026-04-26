package com.deltabox.backend.service.impl;

import com.deltabox.backend.dto.AuthRequest;
import com.deltabox.backend.dto.AuthResponse;
import com.deltabox.backend.dto.GoogleAuthRequest;
import com.deltabox.backend.exception.UserAlreadyExistsException;
import com.deltabox.backend.model.User;
import com.deltabox.backend.repository.UserRepository;
import com.deltabox.backend.security.JwtService;
import com.deltabox.backend.service.AuthService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RestTemplate restTemplate;

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           AuthenticationManager authenticationManager,
                           RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.restTemplate = restTemplate;
    }

    // ✅ REGISTER
    @Override
    public AuthResponse register(AuthRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername() != null ? request.getUsername() : request.getEmail());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");

        userRepository.save(user);

        // ✅ Convert to Spring Security UserDetails
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles("USER")
                .build();

        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(token, user.getEmail(), user.getRole());
    }

    // ✅ LOGIN
    @Override
    public AuthResponse login(AuthRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ Convert to Spring Security UserDetails
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();

        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(token, user.getEmail(), user.getRole());
    }

    // ✅ GOOGLE OAUTH2
    @Override
    public AuthResponse googleAuth(GoogleAuthRequest request) {
        String idToken = request.getIdToken();
        
        // Verify Google ID token with Google's tokeninfo API
        String googleTokenInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                googleTokenInfoUrl,
                HttpMethod.GET,
                null,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> tokenInfo = response.getBody();
                
                String email = (String) tokenInfo.get("email");
                String googleSub = (String) tokenInfo.get("sub");
                String name = (String) tokenInfo.get("name");
                String givenName = (String) tokenInfo.get("given_name");
                String familyName = (String) tokenInfo.get("family_name");
                
                if (email == null || googleSub == null) {
                    throw new RuntimeException("Invalid Google token: missing email or sub");
                }
                
                // Check if user exists by email
                User user = userRepository.findByEmail(email).orElse(null);
                
                if (user == null) {
                    // Create new user
                    user = new User();
                    user.setUsername(givenName != null ? givenName : email);
                    user.setEmail(email);
                    user.setPassword(null); // No password for Google users
                    user.setRole("USER");
                    user.setGoogleSub(googleSub);
                    userRepository.save(user);
                    logger.info("Created new user via Google OAuth: {}", email);
                } else {
                    // Update existing user's Google sub if not set
                    if (user.getGoogleSub() == null) {
                        user.setGoogleSub(googleSub);
                        userRepository.save(user);
                    }
                    logger.info("Existing user logged in via Google OAuth: {}", email);
                }
                
                // Generate JWT token
                UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password("") // No password for Google users
                        .roles(user.getRole())
                        .build();
                
                String token = jwtService.generateToken(userDetails);
                
                return new AuthResponse(token, user.getEmail(), user.getRole());
            } else {
                throw new RuntimeException("Failed to verify Google token");
            }
        } catch (Exception e) {
            logger.error("Google OAuth verification failed: {}", e.getMessage());
            throw new RuntimeException("Google authentication failed: " + e.getMessage());
        }
    }
}
