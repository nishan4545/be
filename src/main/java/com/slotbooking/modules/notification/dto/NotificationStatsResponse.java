package com.slotbooking.modules.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO aggregating system-wide notification metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationStatsResponse {

    private long totalNotifications;
    private long pendingNotifications;
    private long sentNotifications;
    private long deliveredNotifications;
    private long failedNotifications;
    private long readNotifications;
}
