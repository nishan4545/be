package com.slotbooking.modules.auth.controller;

import com.slotbooking.common.response.ApiResponse;
import com.slotbooking.modules.auth.dto.*;
import com.slotbooking.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for all authentication operations.
 * Handles OTP sending/verification, user registration, login, and logout.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Sends an OTP to the specified mobile number.
     *
     * @param request contains the mobile number
     * @return success message confirming OTP dispatch
     */
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@RequestBody @Valid AuthRequest request) {
        authService.sendOtp(request.getMobileNumber());
        return ResponseEntity.ok(ApiResponse.success("OTP sent successfully", null));
    }

    /**
     * Verifies the OTP submitted for a mobile number.
     *
     * @param request contains the mobile number and OTP code
     * @return success message confirming OTP verification
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@RequestBody @Valid VerifyRequest request) {
        authService.verifyOtp(request.getMobileNumber(), request.getOtp());
        return ResponseEntity.ok(ApiResponse.success("OTP verified successfully", null));
    }

    /**
     * Registers a new user. The mobile number must have been OTP verified
     * beforehand. Creates the user with status PENDING and role PLAYER.
     *
     * @param request registration details including mobile, password, name
     * @return success message confirming registration
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody @Valid RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registration successful. Awaiting admin approval.", null));
    }

    /**
     * Authenticates a user with mobile number and password.
     * Only users with APPROVED status are allowed to log in.
     *
     * @param request login credentials
     * @return JWT token and user details
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody @Valid LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * Logs out the current user. With stateless JWT, this is a client-side
     * operation. The endpoint exists for API completeness.
     *
     * @return success message confirming logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }
}
