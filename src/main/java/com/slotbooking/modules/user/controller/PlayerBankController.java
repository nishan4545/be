package com.slotbooking.modules.user.controller;

import com.slotbooking.common.response.ApiResponse;
import com.slotbooking.modules.user.dto.BankAccountResponse;
import com.slotbooking.modules.user.dto.BankDetailsRequest;
import com.slotbooking.modules.user.dto.UpiDetailsRequest;
import com.slotbooking.modules.user.entity.User;
import com.slotbooking.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for player bank account and UPI details CRUD operations.
 * Base URL is /api/player/bank. Restricted to ROLE_PLAYER via SecurityConfig.
 */
@RestController
@RequestMapping("/api/player/bank")
@RequiredArgsConstructor
public class PlayerBankController {

    private final UserService userService;

    /**
     * Adds bank details for the current logged-in player.
     *
     * @param currentUser the authenticated player
     * @param request     the bank account details to add
     * @return response entity wrapping the updated bank account details
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BankAccountResponse>> addBankDetails(
            @AuthenticationPrincipal User currentUser,
            @RequestBody @Valid BankDetailsRequest request
    ) {
        BankAccountResponse response = userService.addBankDetails(currentUser, request);
        return ResponseEntity.ok(ApiResponse.success("Bank details added successfully", response));
    }

    /**
     * Adds UPI details for the current logged-in player.
     *
     * @param currentUser the authenticated player
     * @param request     the UPI details to add
     * @return response entity wrapping the updated bank account details
     */
    @PostMapping("/upi")
    public ResponseEntity<ApiResponse<BankAccountResponse>> addUpiDetails(
            @AuthenticationPrincipal User currentUser,
            @RequestBody @Valid UpiDetailsRequest request
    ) {
        BankAccountResponse response = userService.addUpiDetails(currentUser, request);
        return ResponseEntity.ok(ApiResponse.success("UPI details added successfully", response));
    }

    /**
     * Updates existing bank details by their ID.
     *
     * @param currentUser the authenticated player
     * @param id          the identifier of the bank account
     * @param request     the updated bank details
     * @return response entity wrapping the updated bank account details
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BankAccountResponse>> updateBankDetails(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id,
            @RequestBody @Valid BankDetailsRequest request
    ) {
        BankAccountResponse response = userService.updateBankDetails(currentUser, id, request);
        return ResponseEntity.ok(ApiResponse.success("Bank details updated successfully", response));
    }

    /**
     * Deletes or clears the bank details configuration.
     *
     * @param currentUser the authenticated player
     * @param id          the identifier of the bank account
     * @return response entity confirming deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBankDetails(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id
    ) {
        userService.deleteBankDetails(currentUser, id);
        return ResponseEntity.ok(ApiResponse.success("Bank details deleted successfully", null));
    }

    /**
     * Retrieves the bank and UPI details configurations for the current logged-in player.
     *
     * @param currentUser the authenticated player
     * @return response entity wrapping the bank details response
     */
    @GetMapping
    public ResponseEntity<ApiResponse<BankAccountResponse>> getBankDetails(
            @AuthenticationPrincipal User currentUser
    ) {
        BankAccountResponse response = userService.getBankDetails(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Bank details retrieved successfully", response));
    }
}
