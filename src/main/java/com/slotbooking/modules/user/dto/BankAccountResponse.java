package com.slotbooking.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returning bank account and UPI details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountResponse {

    /** Unique identifier of the bank account configuration */
    private Long id;

    /** Bank account holder name */
    private String bankAccountHolderName;

    /** Bank name */
    private String bankName;

    /** Bank account number */
    private String accountNumber;

    /** IFSC code */
    private String ifscCode;

    /** Branch name */
    private String branchName;

    /** UPI ID */
    private String upiId;

    /** UPI account holder name */
    private String upiAccountHolderName;
}
