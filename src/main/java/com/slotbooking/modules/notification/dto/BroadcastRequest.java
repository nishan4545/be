package com.slotbooking.modules.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO containing details for global broadcasts sent by administrators.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BroadcastRequest {

    @NotBlank(message = "Title must not be blank")
    private String title;

    @NotBlank(message = "Message must not be blank")
    private String message;
}
