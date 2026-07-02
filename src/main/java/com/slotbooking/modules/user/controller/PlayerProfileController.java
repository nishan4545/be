package com.slotbooking.modules.user.controller;

import com.slotbooking.common.response.ApiResponse;
import com.slotbooking.modules.user.dto.UserDetailsResponse;
import com.slotbooking.modules.user.dto.UserProfileUpdateRequest;
import com.slotbooking.modules.user.entity.User;
import com.slotbooking.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for players to manage their own profile details.
 * Base URL is /api/player/profile. Restricted to ROLE_PLAYER via SecurityConfig.
 */
@RestController
@RequestMapping("/api/player/profile")
@RequiredArgsConstructor
public class PlayerProfileController {

    private final UserService userService;

    /**
     * Retrieves the profile details of the current authenticated player.
     *
     * @param currentUser the authenticated player user principal
     * @return response entity wrapping the profile details
     */
    @GetMapping
    public ResponseEntity<ApiResponse<UserDetailsResponse>> getProfile(
            @AuthenticationPrincipal User currentUser
    ) {
        UserDetailsResponse profile = userService.getProfile(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
    }

    /**
     * Updates the profile of the current authenticated player.
     * Allows updating Full Name, Email, and Profile Image.
     *
     * @param currentUser the authenticated player user principal
     * @param request     the profile update details
     * @return response entity wrapping the updated profile details
     */
    @PutMapping
    public ResponseEntity<ApiResponse<UserDetailsResponse>> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @RequestBody @Valid UserProfileUpdateRequest request
    ) {
        UserDetailsResponse updatedProfile = userService.updateProfile(currentUser, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedProfile));
    }
}
