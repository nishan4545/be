package com.slotbooking.modules.notification.controller;

import com.slotbooking.common.response.ApiResponse;
import com.slotbooking.modules.notification.dto.BroadcastRequest;
import com.slotbooking.modules.notification.dto.NotificationResponse;
import com.slotbooking.modules.notification.dto.NotificationStatsResponse;
import com.slotbooking.modules.notification.service.NotificationService;
import com.slotbooking.modules.notification.service.RetryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for admin notifications dashboard reporting and broadcast.
 * Base URL is /api/admin/notifications. Restricted to ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final NotificationService notificationService;
    private final RetryService retryService;

    /**
     * Lists all system notifications with pagination.
     *
     * @param pageable pagination parameters
     * @return response entity wrapping the notifications page
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getAllNotifications(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<NotificationResponse> notifications = notificationService.getAllNotifications(pageable);
        return ResponseEntity.ok(ApiResponse.success("All notifications retrieved successfully", notifications));
    }

    /**
     * Gathers stats for all delivery channels.
     *
     * @return response entity wrapping dashboard stats
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<NotificationStatsResponse>> getStatistics() {
        NotificationStatsResponse stats = notificationService.getStatistics();
        return ResponseEntity.ok(ApiResponse.success("System notification statistics retrieved", stats));
    }

    /**
     * Lists all failed notifications.
     *
     * @return response entity wrapping the list of failed notifications
     */
    @GetMapping("/failed")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getFailedNotifications() {
        List<NotificationResponse> failed = notificationService.getFailedNotifications();
        return ResponseEntity.ok(ApiResponse.success("Failed notifications list retrieved", failed));
    }

    /**
     * Retries a specific failed notification manually.
     *
     * @param id notification identifier to retry
     * @return empty response entity indicating retry triggered
     */
    @PostMapping("/retry/{id}")
    public ResponseEntity<ApiResponse<Void>> retryNotification(
            @PathVariable UUID id
    ) {
        retryService.retryNotification(id);
        return ResponseEntity.ok(ApiResponse.success("Retry command dispatched", null));
    }

    /**
     * Broadcasts announcement title and message to all registered players.
     *
     * @param request announcement details
     * @return empty response entity indicating broadcast triggered
     */
    @PostMapping("/broadcast")
    public ResponseEntity<ApiResponse<Void>> broadcastAnnouncement(
            @RequestBody @Valid BroadcastRequest request
    ) {
        notificationService.broadcastAnnouncement(request);
        return ResponseEntity.ok(ApiResponse.success("Announcement broadcast successfully", null));
    }
}
