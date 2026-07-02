package com.slotbooking.modules.payment.controller;

import com.slotbooking.common.response.ApiResponse;
import com.slotbooking.modules.payment.dto.CreateOrderRequest;
import com.slotbooking.modules.payment.dto.OrderResponse;
import com.slotbooking.modules.payment.dto.PaymentResponse;
import com.slotbooking.modules.payment.dto.PaymentVerificationRequest;
import com.slotbooking.modules.payment.service.PaymentService;
import com.slotbooking.modules.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for player payment operations.
 * Base URL is /api/player/payments. Restricted to ROLE_PLAYER via SecurityConfig.
 */
@RestController
@RequestMapping("/api/player/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Initiates a payment order with Razorpay.
     * Generates a unique Razorpay order ID to initialize the checkout modal on the client.
     *
     * @param currentUser the authenticated player
     * @param request     contains the booking ID to pay for
     * @return response entity wrapping the Razorpay order details
     */
    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal User currentUser,
            @RequestBody @Valid CreateOrderRequest request
    ) {
        OrderResponse response = paymentService.createOrder(currentUser, request);
        return ResponseEntity.ok(ApiResponse.success("Order created successfully", response));
    }

    /**
     * Verifies the Razorpay payment signature after successful checkout.
     *
     * @param currentUser the authenticated player
     * @param request     contains payment ID, order ID, and signature
     * @return response entity wrapping the success payment details
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(
            @AuthenticationPrincipal User currentUser,
            @RequestBody @Valid PaymentVerificationRequest request
    ) {
        PaymentResponse response = paymentService.verifyPayment(currentUser, request);
        return ResponseEntity.ok(ApiResponse.success("Payment verified successfully", response));
    }

    /**
     * Retrieves the payment transaction history of the current player.
     *
     * @param currentUser the authenticated player
     * @return response entity wrapping the list of player payments
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getMyPaymentHistory(
            @AuthenticationPrincipal User currentUser
    ) {
        List<PaymentResponse> history = paymentService.getMyPaymentHistory(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Payment history retrieved successfully", history));
    }

    /**
     * Retrieves detailed information of a specific payment transaction.
     *
     * @param currentUser the authenticated player
     * @param id          the identifier of the payment transaction
     * @return response entity wrapping the detailed payment response
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentDetails(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id
    ) {
        PaymentResponse details = paymentService.getPaymentDetails(currentUser, id);
        return ResponseEntity.ok(ApiResponse.success("Payment details retrieved successfully", details));
    }
}
