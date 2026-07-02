package com.slotbooking.modules.notification.entity;

import com.slotbooking.modules.notification.enums.DeliveryChannel;
import com.slotbooking.modules.notification.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity mapping the database notification delivery logs table.
 * Tracks attempts, provider response codes, and retry statistics for each channel.
 */
@Entity
@Table(name = "notification_delivery_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDeliveryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryChannel channel;

    @Column(nullable = false, length = 50)
    private String provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(columnDefinition = "TEXT")
    private String response;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
