package com.slotbooking.modules.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Event DTO representing player payment transaction updates sent privately to individual sessions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {

    /** Event type: e.g., SUCCESS, FAILED, REFUND_SUCCESS */
    private EventType eventType;

    /** Unique identifier of the payment record */
    private Long paymentId;

    /** Associated booking identifier */
    private Long bookingId;

    /** Razorpay order identifier */
    private String razorpayOrderId;

    /** Transaction amount */
    private BigDecimal amount;

    /** Current payment status */
    private String status;
}
