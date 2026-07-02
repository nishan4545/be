package com.slotbooking.modules.notification.service;

import com.slotbooking.modules.notification.dto.DeviceTokenRequest;
import com.slotbooking.modules.notification.entity.DeviceToken;
import com.slotbooking.modules.notification.enums.Platform;
import com.slotbooking.modules.notification.provider.PushNotificationProvider;
import com.slotbooking.modules.notification.repository.DeviceTokenRepository;
import com.slotbooking.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service implementation for PushNotificationService.
 * Coordinates device registrations and Firebase push alerts asynchronously.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationServiceImpl implements PushNotificationService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final PushNotificationProvider pushNotificationProvider;

    @Override
    @Transactional
    public void registerDevice(User currentUser, DeviceTokenRequest request) {
        log.info("[Push Service] Registering device token for user: {}", currentUser.getMobileNumber());
        
        DeviceToken deviceToken = deviceTokenRepository.findByDeviceToken(request.getToken())
                .orElseGet(() -> DeviceToken.builder()
                        .deviceToken(request.getToken())
                        .build());
                        
        deviceToken.setUser(currentUser);
        deviceToken.setPlatform(Platform.valueOf(request.getPlatform().toUpperCase()));
        deviceToken.setActive(true);
        deviceToken.setCreatedAt(LocalDateTime.now());
        
        deviceTokenRepository.save(deviceToken);
        log.info("[Push Service] Device token successfully registered.");
    }

    @Override
    @Transactional
    public void unregisterDevice(User currentUser, String token) {
        log.info("[Push Service] Unregistering device token for user: {}", currentUser.getMobileNumber());
        deviceTokenRepository.findByDeviceToken(token).ifPresent(deviceToken -> {
            if (deviceToken.getUser().getId().equals(currentUser.getId())) {
                deviceToken.setActive(false);
                deviceTokenRepository.save(deviceToken);
                log.info("[Push Service] Device token successfully unregistered.");
            }
        });
    }

    @Async
    @Override
    @Transactional(readOnly = true)
    public void sendPushNotification(Long userId, String title, String body) {
        List<DeviceToken> tokens = deviceTokenRepository.findByUserIdAndActiveTrue(userId);
        log.info("[Push Service] Found {} active device token(s) for user ID: {}", tokens.size(), userId);
        
        for (DeviceToken deviceToken : tokens) {
            try {
                pushNotificationProvider.sendPush(deviceToken.getDeviceToken(), title, body);
            } catch (Exception e) {
                log.error("[Push Service] Failed to send push notification to token: {}, error: {}", 
                        deviceToken.getDeviceToken(), e.getMessage());
            }
        }
    }
}
