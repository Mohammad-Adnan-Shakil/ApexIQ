package com.f1pulse.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * ✅ Production-Ready JWT Service
 * 
 * Key Fixes:
 * 1. SECRET_KEY is now fixed (from properties) - NOT regenerated on restart
 * 2. Uses proper JJWT parsing with no manual Base64 decoding
 * 3. Token expiration is configurable
 * 4. HS256 algorithm ensures consistency
 * 5. Exception-safe token extraction
 */
@Service
public class JwtService {

    private final SecretKey SECRET_KEY;
    private final long JWT_EXPIRATION;

    public JwtService(@Value("${jwt.secret}") String secretString,
                     @Value("${jwt.expiration}") long jwtExpiration) {
        // 🔑 CRITICAL: Create key from fixed string (NOT generated)
        // The secret must be at least 256 bits (32 bytes) for HS256
        this.SECRET_KEY = Keys.hmacShaKeyFor(secretString.getBytes());
        this.JWT_EXPIRATION = jwtExpiration;
    }

    /**
     * Extract username from token
     * @throws io.jsonwebtoken.JwtException if token is invalid
     */
    public String extractUsername(String token) {
        try {
            return extractAllClaims(token).getSubject();
        } catch (Exception e) {
            // Token is invalid - return null instead of throwing
            return null;
        }
    }

    /**
     * Generate JWT token with HS256
     * Token includes:
     * - subject (username/email)
     * - issuedAt (current time)
     * - expiration (configured duration)
     */
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validate token against UserDetails
     * Checks:
     * - Username matches
     * - Token is not expired
     * - Token signature is valid
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username != null && 
                   username.equals(userDetails.getUsername()) && 
                   !isTokenExpired(token);
        } catch (Exception e) {
            // Token parsing/validation failed
            return false;
        }
    }

    /**
     * Check if token has expired
     */
    private boolean isTokenExpired(String token) {
        try {
            Date expiration = extractAllClaims(token).getExpiration();
            return expiration != null && expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Extract all claims from token
     * ✅ NO manual Base64 decoding - uses proper JJWT parsing
     * 
     * @throws io.jsonwebtoken.JwtException if token is invalid or signature doesn't match
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
