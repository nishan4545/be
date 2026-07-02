package com.slotbooking.modules.notification.entity;

import com.slotbooking.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Entity mapping the database notification preferences table.
 * Controls which delivery channels a user has opted into.
 */
@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled;

    @Column(name = "push_enabled", nullable = false)
    private boolean pushEnabled;

    @Column(name = "sms_enabled", nullable = false)
    private boolean smsEnabled;

    @Column(name = "websocket_enabled", nullable = false)
    private boolean websocketEnabled;

    @Column(name = "in_app_enabled", nullable = false)
    private boolean inAppEnabled;
}
