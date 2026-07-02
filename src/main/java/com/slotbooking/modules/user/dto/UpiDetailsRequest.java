package com.slotbooking.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO containing UPI details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpiDetailsRequest {

    /** UPI ID of the player, e.g., user@upi */
    @NotBlank(message = "UPI ID must not be blank")
    @Pattern(regexp = "^[\\w.-]+@[\\w.-]+$", message = "Invalid UPI format")
    private String upiId;

    /** Name of the UPI account holder */
    @NotBlank(message = "Account holder name must not be blank")
    private String accountHolderName;
}
