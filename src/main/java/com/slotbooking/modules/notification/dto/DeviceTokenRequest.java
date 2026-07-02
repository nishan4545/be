package com.slotbooking.modules.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO mapping client device token registration details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTokenRequest {

    @NotBlank(message = "Device token is required")
    private String token;

    @NotBlank(message = "Platform is required (ANDROID, IOS, WEB)")
    private String platform;
}
