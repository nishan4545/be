package com.slotbooking.modules.notification.entity;

import com.slotbooking.modules.notification.enums.DeliveryChannel;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Entity mapping the database notification templates table.
 */
@Entity
@Table(name = "notification_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "template_name", unique = true, nullable = false, length = 100)
    private String templateName;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryChannel channel;

    @Column(nullable = false)
    private boolean active;
}
