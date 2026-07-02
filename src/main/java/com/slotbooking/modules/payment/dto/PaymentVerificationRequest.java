package com.slotbooking.modules.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO containing Razorpay verification credentials after payment success.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVerificationRequest {

    /** Razorpay order ID received during order creation */
    @NotBlank(message = "Razorpay Order ID must not be blank")
    private String razorpayOrderId;

    /** Razorpay payment ID received from checkout modal */
    @NotBlank(message = "Razorpay Payment ID must not be blank")
    private String razorpayPaymentId;

    /** Razorpay signature generated for authenticity check */
    @NotBlank(message = "Razorpay Signature must not be blank")
    private String razorpaySignature;
}
