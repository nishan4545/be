package com.slotbooking.security;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

/**
 * Interface for JWT token generation and validation operations.
 */
public interface TokenService {
    String extractUsername(String token);
    String generateToken(UserDetails userDetails);
    String generateToken(Map<String, Object> extraClaims, UserDetails userDetails);
    boolean isTokenValid(String token, UserDetails userDetails);
}
