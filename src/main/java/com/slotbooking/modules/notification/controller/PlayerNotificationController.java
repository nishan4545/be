package com.slotbooking.modules.notification.controller;

import com.slotbooking.common.response.ApiResponse;
import com.slotbooking.exception.BusinessException;
import com.slotbooking.exception.ResourceNotFoundException;
import com.slotbooking.modules.notification.dto.NotificationResponse;
import com.slotbooking.modules.notification.entity.Notification;
import com.slotbooking.modules.notification.repository.NotificationRepository;
import com.slotbooking.modules.notification.service.NotificationService;
import com.slotbooking.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for player notifications history and mark actions.
 * Base URL is /api/player/notifications. Restricted to ROLE_PLAYER.
 */
@RestController
@RequestMapping("/api/player/notifications")
@RequiredArgsConstructor
public class PlayerNotificationController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    /**
     * Lists notifications history for the logged-in player.
     *
     * @param currentUser authenticated player
     * @param pageable    pagination options
     * @return response entity wrapping the page of notifications
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotificationHistory(
            @AuthenticationPrincipal User currentUser,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<NotificationResponse> history = notificationService.getNotificationHistory(currentUser, pageable);
        return ResponseEntity.ok(ApiResponse.success("Notifications history retrieved", history));
    }

    /**
     * Retrieves detailed data for a specific notification.
     *
     * @param currentUser authenticated player
     * @param id          notification identifier
     * @return response entity wrapping notification details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotificationDetails(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id
    ) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Access denied. This notification does not belong to you.");
        }

        NotificationResponse response = NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .notificationType(notification.getNotificationType().name())
                .deliveryChannel(notification.getDeliveryChannel().name())
                .status(notification.getStatus().name())
                .priority(notification.getPriority().name())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Notification details retrieved", response));
    }

    /**
     * Marks a specific notification as read.
     *
     * @param currentUser authenticated player
     * @param id          notification identifier
     * @return response entity indicating success
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id
    ) {
        notificationService.markAsRead(currentUser, id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", null));
    }

    /**
     * Marks all unread player notifications as read.
     *
     * @param currentUser authenticated player
     * @return response entity indicating success
     */
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal User currentUser
    ) {
        notificationService.markAllAsRead(currentUser);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }

    /**
     * Deletes a player notification record.
     *
     * @param currentUser authenticated player
     * @param id          notification identifier
     * @return response entity indicating success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id
    ) {
        notificationService.deleteNotification(currentUser, id);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully", null));
    }

    /**
     * Gets the count of unread player notifications.
     *
     * @param currentUser authenticated player
     * @return response entity wrapping the unread count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal User currentUser
    ) {
        long count = notificationService.getUnreadCount(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved", count));
    }
}
