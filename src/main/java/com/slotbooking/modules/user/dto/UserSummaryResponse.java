package com.slotbooking.modules.user.dto;

import com.slotbooking.modules.user.enums.Role;
import com.slotbooking.modules.user.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO for user summary information returned in lists and searches.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {

    /** Unique identifier of the user */
    private Long id;

    /** Full name of the user */
    private String fullName;

    /** Mobile number of the user */
    private String mobileNumber;

    /** Email of the user */
    private String email;

    /** Role of the user */
    private Role role;

    /** User current status */
    private UserStatus status;

    /** Registration timestamp of the user */
    private LocalDateTime createdAt;
}
