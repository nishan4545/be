package com.slotbooking.modules.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO containing channel preference flags to update.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceRequest {

    private Boolean emailEnabled;
    private Boolean pushEnabled;
    private Boolean smsEnabled;
    private Boolean websocketEnabled;
    private Boolean inAppEnabled;
}
