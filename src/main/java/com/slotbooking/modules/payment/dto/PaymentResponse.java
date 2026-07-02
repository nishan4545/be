package com.slotbooking.modules.payment.dto;

import com.slotbooking.modules.payment.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO returning complete details of a payment transaction.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    /** Unique identifier of the payment record */
    private Long id;

    /** Associated booking identifier */
    private Long bookingId;
    private Long tournamentId;
    private String tournamentTitle;
    private String playerFullName;
    private String playerMobileNumber;

    /** Gateway references */
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    /** Transaction information */
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String gateway;
    private LocalDateTime transactionTime;

    /** Audit fields */
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
