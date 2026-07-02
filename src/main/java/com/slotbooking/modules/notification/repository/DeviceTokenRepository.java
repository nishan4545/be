package com.slotbooking.modules.notification.repository;

import com.slotbooking.modules.notification.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for DeviceToken entity operations.
 */
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, UUID> {

    List<DeviceToken> findByUserIdAndActiveTrue(Long userId);

    Optional<DeviceToken> findByDeviceToken(String token);
}
