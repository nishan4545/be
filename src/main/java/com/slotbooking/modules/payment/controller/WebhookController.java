package com.slotbooking.modules.payment.controller;

import com.slotbooking.common.response.ApiResponse;
import com.slotbooking.modules.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for receiving and processing payment gateway webhooks.
 * Base URL is /api/webhooks/razorpay. Publicly accessible.
 */
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final PaymentService paymentService;

    /**
     * Receives event notifications from Razorpay.
     * Validates signature authenticity using the X-Razorpay-Signature header before processing.
     *
     * @param payload   the raw request body JSON payload
     * @param signature the cryptographic signature supplied in header
     * @return response entity indicating event processed status
     */
    @PostMapping("/razorpay")
    public ResponseEntity<ApiResponse<Void>> handleRazorpayWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature
    ) {
        log.info("Received Razorpay Webhook notification");
        try {
            paymentService.processWebhook(payload, signature);
            return ResponseEntity.ok(ApiResponse.success("Webhook event processed successfully", null));
        } catch (Exception e) {
            log.error("Webhook event processing failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Webhook processing failed: " + e.getMessage()));
        }
    }
}
