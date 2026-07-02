package com.slotbooking.modules.payment.controller;

import com.slotbooking.common.response.ApiResponse;
import com.slotbooking.modules.payment.dto.PaymentResponse;
import com.slotbooking.modules.payment.service.PaymentService;
import com.slotbooking.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for admin payment dashboard and reporting.
 * Base URL is /api/admin/payments. Restricted to ROLE_ADMIN via SecurityConfig.
 */
@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentService paymentService;

    /**
     * Lists all payment records in the system.
     *
     * @return response entity wrapping the full list of payments
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAllPayments() {
        List<PaymentResponse> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(ApiResponse.success("All payments retrieved successfully", payments));
    }

    /**
     * Retrieves a single payment record by ID.
     *
     * @param currentUser the authenticated admin user principal
     * @param id          the identifier of the payment record
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

    /**
     * Retrieves all payments associated with a specific tournament.
     *
     * @param tournamentId the identifier of the tournament
     * @return response entity wrapping the list of tournament payments
     */
    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getTournamentPayments(
            @PathVariable Long tournamentId
    ) {
        List<PaymentResponse> payments = paymentService.getTournamentPayments(tournamentId);
        return ResponseEntity.ok(ApiResponse.success("Tournament payments retrieved successfully", payments));
    }

    /**
     * Retrieves booking payment history registered by a specific player.
     *
     * @param playerId the identifier of the player
     * @return response entity wrapping the list of player payments
     */
    @GetMapping("/player/{playerId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPlayerPayments(
            @PathVariable Long playerId
    ) {
        List<PaymentResponse> payments = paymentService.getPlayerPayments(playerId);
        return ResponseEntity.ok(ApiResponse.success("Player payment history retrieved successfully", payments));
    }

    /**
     * Processes a manual/admin-initiated refund for a transaction.
     * Restores slots and transitions states.
     *
     * @param id the identifier of the payment transaction to refund
     * @return response entity indicating refund success
     */
    @PostMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<Void>> processRefund(
            @PathVariable Long id
    ) {
        paymentService.processRefund(id);
        return ResponseEntity.ok(ApiResponse.success("Refund processed successfully", null));
    }
}
