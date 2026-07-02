package com.slotbooking.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO containing bank details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankDetailsRequest {

    /** Name of the bank account holder */
    @NotBlank(message = "Account holder name must not be blank")
    private String accountHolderName;

    /** Name of the bank */
    @NotBlank(message = "Bank name must not be blank")
    private String bankName;

    /** Bank account number */
    @NotBlank(message = "Account number must not be blank")
    private String accountNumber;

    /** Bank IFSC code, must match standard 11 character format */
    @NotBlank(message = "IFSC code must not be blank")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC format")
    private String ifscCode;

    /** Branch name of the bank */
    @NotBlank(message = "Branch name must not be blank")
    private String branchName;
}
