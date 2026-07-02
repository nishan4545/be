package com.slotbooking.modules.user.dto;

import com.slotbooking.modules.user.enums.Role;
import com.slotbooking.modules.user.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Detailed profile details containing user information, bank details, and UPI details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsResponse {

    /** User ID */
    private Long id;

    /** Full Name */
    private String fullName;

    /** Mobile Number */
    private String mobileNumber;

    /** Email Address */
    private String email;

    /** User Role */
    private Role role;

    /** User Status */
    private UserStatus status;

    /** Profile Photo URL or identifier */
    private String profilePhoto;

    /** Bank Account Holder Name */
    private String bankAccountHolderName;

    /** Bank Name */
    private String bankName;

    /** Bank Account Number */
    private String accountNumber;

    /** Bank IFSC Code */
    private String ifscCode;

    /** Bank Branch Name */
    private String branchName;

    /** UPI ID */
    private String upiId;

    /** UPI Account Holder Name */
    private String upiAccountHolderName;

    /** Timestamp when user registered */
    private LocalDateTime createdAt;

    /** Timestamp when user status was approved */
    private LocalDateTime approvedAt;

    /** Identification of the Admin who approved the user */
    private String approvedBy;
}
