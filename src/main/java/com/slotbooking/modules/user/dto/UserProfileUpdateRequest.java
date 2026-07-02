package com.slotbooking.modules.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a player's own profile.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequest {

    /** Updated full name of the player. Must not be empty. */
    @NotBlank(message = "Full name must not be blank")
    private String fullName;

    /** Updated email of the player. Must be valid. */
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    private String email;

    /** Updated profile photo/image URL or key */
    private String profileImage;
}
