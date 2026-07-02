package com.slotbooking.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned after successful login.
 * Contains the JWT token, user role, and basic user details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String role;
    private Long userId;
    private String fullName;
    private String mobileNumber;
    private String status;
}
