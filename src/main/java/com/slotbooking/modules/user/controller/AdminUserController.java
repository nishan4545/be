package com.slotbooking.modules.user.controller;

import com.slotbooking.common.response.ApiResponse;
import com.slotbooking.modules.user.dto.UserDetailsResponse;
import com.slotbooking.modules.user.dto.UserSummaryResponse;
import com.slotbooking.modules.user.entity.User;
import com.slotbooking.modules.user.enums.Role;
import com.slotbooking.modules.user.enums.UserStatus;
import com.slotbooking.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for admin user management.
 * Base URL is /api/admin/users. Restricted to ROLE_ADMIN via SecurityConfig.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    /**
     * Retrieves a paginated list of players filtered by search parameters.
     *
     * @param search       optional search keyword (matches name, mobile number, or email)
     * @param status       optional user status filter
     * @param role         optional user role filter
     * @param page         page index (0-indexed, default is 0)
     * @param size         page size limit (default is 10)
     * @return response entity wrapping the paginated user summaries
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserSummaryResponse>>> getPlayerList(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserSummaryResponse> userPage = userService.getPlayerList(search, status, role, pageRequest);
        return ResponseEntity.ok(ApiResponse.success("Users list retrieved successfully", userPage));
    }

    /**
     * Retrieves detailed profile and account details for a specific player by ID.
     *
     * @param id the user ID
     * @return response entity wrapping user detailed information
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDetailsResponse>> getPlayerDetails(@PathVariable Long id) {
        UserDetailsResponse details = userService.getPlayerDetails(id);
        return ResponseEntity.ok(ApiResponse.success("User details retrieved successfully", details));
    }

    /**
     * Approves a pending player.
     * Sets status = APPROVED, approvedBy = Current Admin, approvedAt = current timestamp.
     *
     * @param id    the user ID to approve
     * @param admin the current admin user principal
     * @return response entity indicating approval success
     */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approvePlayer(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin
    ) {
        userService.approvePlayer(id, admin);
        return ResponseEntity.ok(ApiResponse.success("User approved successfully", null));
    }

    /**
     * Rejects a player registration.
     * Sets status = REJECTED.
     *
     * @param id the user ID to reject
     * @return response entity indicating rejection success
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectPlayer(@PathVariable Long id) {
        userService.rejectPlayer(id);
        return ResponseEntity.ok(ApiResponse.success("User registration rejected", null));
    }

    /**
     * Blocks a player.
     * Sets status = BLOCKED. Blocked users cannot log in.
     *
     * @param id the user ID to block
     * @return response entity indicating blocking success
     */
    @PatchMapping("/{id}/block")
    public ResponseEntity<ApiResponse<Void>> blockPlayer(@PathVariable Long id) {
        userService.blockPlayer(id);
        return ResponseEntity.ok(ApiResponse.success("User blocked successfully", null));
    }

    /**
     * Unblocks a player.
     * Sets status = APPROVED.
     *
     * @param id the user ID to unblock
     * @return response entity indicating unblocking success
     */
    @PatchMapping("/{id}/unblock")
    public ResponseEntity<ApiResponse<Void>> unblockPlayer(@PathVariable Long id) {
        userService.unblockPlayer(id);
        return ResponseEntity.ok(ApiResponse.success("User unblocked successfully", null));
    }
}
