package com.slotbooking.modules.notification.entity;

import com.slotbooking.modules.notification.enums.Platform;
import com.slotbooking.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity mapping the database device tokens table.
 * Tracks active Firebase Cloud Messaging (FCM) push notification tokens.
 */
@Entity
@Table(name = "device_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "device_token", nullable = false, columnDefinition = "TEXT")
    private String deviceToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    @Column(nullable = false)
    private boolean active;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
