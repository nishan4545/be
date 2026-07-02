package com.slotbooking.modules.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO to initiate a payment order for a booking.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    /** The identifier of the booking awaiting payment */
    @NotNull(message = "Booking ID must not be null")
    private Long bookingId;
}
