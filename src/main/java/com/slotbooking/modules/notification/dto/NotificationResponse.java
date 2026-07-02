package com.slotbooking.modules.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO returning details of a notification record.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private UUID id;
    private String title;
    private String message;
    private String notificationType;
    private String deliveryChannel;
    private String status;
    private String priority;
    private String referenceType;
    private Long referenceId;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
