package com.f1pulse.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ✅ Production-Ready JWT Authentication Filter
 * 
 * Responsibilities:
 * 1. Extract JWT token from Authorization header
 * 2. Validate token using JwtService
 * 3. Load user details using UserDetailsService
 * 4. Set authentication in SecurityContext
 * 5. Handle errors gracefully without breaking filter chain
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService,
                         CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 🔍 Step 1: Extract token from Authorization header
            final String authHeader = request.getHeader("Authorization");
            
            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                // No token - proceed without authentication
                filterChain.doFilter(request, response);
                return;
            }

            // 🔍 Step 2: Extract JWT token
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();
            
            if (token.isEmpty()) {
                logger.debug("Empty token provided");
                filterChain.doFilter(request, response);
                return;
            }

            // 🔍 Step 3: Extract username from token
            String email = jwtService.extractUsername(token);

            if (email == null) {
                logger.debug("Could not extract username from token");
                filterChain.doFilter(request, response);
                return;
            }

            // 🔍 Step 4: Check if user is already authenticated
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                logger.debug("User already authenticated: {}", email);
                filterChain.doFilter(request, response);
                return;
            }

            // 🔍 Step 5: Load user details from database
            UserDetails userDetails;
            try {
                userDetails = userDetailsService.loadUserByUsername(email);
            } catch (UsernameNotFoundException e) {
                logger.debug("User not found: {}", email);
                filterChain.doFilter(request, response);
                return;
            }

            // 🔍 Step 6: Validate token
            if (!jwtService.isTokenValid(token, userDetails)) {
                logger.debug("Token validation failed for user: {}", email);
                filterChain.doFilter(request, response);
                return;
            }

            // ✅ Step 7: Token is valid - Create authentication token
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            // Add request details (IP, session ID, etc.)
            authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            // ✅ Step 8: Set authentication in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authToken);
            logger.debug("Authentication set for user: {} with authorities: {}", 
                    email, userDetails.getAuthorities());

        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
            // Don't break the filter chain on error
        }

        // ✅ Continue filter chain
        filterChain.doFilter(request, response);
    }
}
