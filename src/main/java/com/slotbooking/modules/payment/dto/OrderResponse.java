package com.slotbooking.modules.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Response DTO returned to frontend to initialize the Razorpay checkout modal.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    /** Razorpay order identifier */
    private String razorpayOrderId;

    /** Amount of the transaction */
    private BigDecimal amount;

    /** Currency code, e.g., INR */
    private String currency;

    /** Public key identifier to boot checkout client */
    private String keyId;
}
