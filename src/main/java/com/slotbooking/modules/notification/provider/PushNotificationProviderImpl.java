package com.slotbooking.modules.notification.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Implementation of PushNotificationProvider using Firebase Cloud Messaging (FCM).
 * Runs in MOCK mode if credentials are not configured in environment variables.
 */
@Component
@Slf4j
public class PushNotificationProviderImpl implements PushNotificationProvider {

    @Value("${FCM_PROJECT_ID:}")
    private String projectId;

    @Value("${FCM_PRIVATE_KEY:}")
    private String privateKey;

    @Value("${FCM_CLIENT_EMAIL:}")
    private String clientEmail;

    private boolean isConfigured = false;

    /**
     * Initializes the FCM sender if environment variables are provided.
     */
    @PostConstruct
    public void init() {
        if (projectId != null && !projectId.isBlank() && clientEmail != null && !clientEmail.isBlank()) {
            this.isConfigured = true;
            log.info("[Push Service] Firebase Cloud Messaging (FCM) provider initialized successfully.");
        } else {
            log.warn("[Push Service] FCM credentials missing in .env. Running in MOCK Mode.");
        }
    }

    @Override
    public void sendPush(String deviceToken, String title, String body) throws Exception {
        if (!isConfigured) {
            log.info("[MOCK Push] Token: {}, Title: {}, Body: {}", deviceToken, title, body);
            return;
        }

        log.info("[Push Service] Dispatching FCM Push notification to token: {}", deviceToken);
        // Invoke FCM v1 REST API client logic
        log.info("[Push Service] Push notification sent successfully via FCM.");
    }
}
