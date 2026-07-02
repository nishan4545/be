package com.slotbooking.modules.payment.service;

import com.slotbooking.modules.payment.dto.CreateOrderRequest;
import com.slotbooking.modules.payment.dto.OrderResponse;
import com.slotbooking.modules.payment.dto.PaymentResponse;
import com.slotbooking.modules.payment.dto.PaymentVerificationRequest;
import com.slotbooking.modules.user.entity.User;

import java.util.List;

/**
 * Service interface managing Razorpay orders and payment transactions.
 */
public interface PaymentService {

    /**
     * Creates a Razorpay order for a booking.
     * Enforces that the booking belongs to the player and status is PENDING_PAYMENT.
     *
     * @param currentUser the authenticated player user principal
     * @param request     contains the booking ID
     * @return response containing the generated Razorpay order ID
     */
    OrderResponse createOrder(User currentUser, CreateOrderRequest request);

    /**
     * Verifies the Razorpay payment signature after successful player checkout.
     * Updates payment status, locks the tournament slot, and confirms the booking.
     *
     * @param currentUser the authenticated player user principal
     * @param request     contains payment ID, order ID, and signature
     * @return the verified payment transaction details
     */
    PaymentResponse verifyPayment(User currentUser, PaymentVerificationRequest request);

    /**
     * Confirms a payment transaction by Razorpay Order ID.
     */
    void confirmPaymentTransaction(String orderId, String paymentId, String signature);

    /**
     * Processes payment webhook events dispatched by Razorpay.
     * Handles payment.captured, payment.failed, and refund.processed events.
     *
     * @param payload   the raw JSON string payload received in HTTP request body
     * @param signature the webhook signature from the header
     */
    void processWebhook(String payload, String signature);

    /**
     * Marks a payment and booking as failed when payment fails.
     * Releases any slot constraints associated with the booking.
     *
     * @param orderId   the Razorpay order identifier
     * @param paymentId the Razorpay payment identifier
     * @param reason    description of the failure
     */
    void handlePaymentFailure(String orderId, String paymentId, String reason);

    /**
     * Retrieves payment history for the currently logged-in player.
     *
     * @param currentUser the authenticated player
     * @return list of payment responses
     */
    List<PaymentResponse> getMyPaymentHistory(User currentUser);

    /**
     * Retrieves detailed payment transaction info by ID.
     * Verifies that the payment record belongs to the player or that user is admin.
     *
     * @param currentUser the authenticated user principal
     * @param id          the payment identifier
     * @return payment details response DTO
     */
    PaymentResponse getPaymentDetails(User currentUser, Long id);

    /**
     * Lists all payment records in the system.
     * Accessible by admins.
     *
     * @return list of all payment records
     */
    List<PaymentResponse> getAllPayments();

    /**
     * Lists all payment records associated with a specific tournament.
     * Accessible by admins.
     *
     * @param tournamentId the tournament identifier
     * @return list of payments for the tournament
     */
    List<PaymentResponse> getTournamentPayments(Long tournamentId);

    /**
     * Lists all payments registered by a specific player.
     * Accessible by admins.
     *
     * @param playerId the player identifier
     * @return list of payments registered by the player
     */
    List<PaymentResponse> getPlayerPayments(Long playerId);

    /**
     * Prepares and executes a refund for a successful booking payment.
     * Sets payment to REFUNDED, booking to CANCELLED, and releases slots.
     * Future-ready endpoint.
     *
     * @param paymentId the identifier of the payment record to refund
     */
    void processRefund(Long paymentId);
}
