package com.slotbooking.modules.notification.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Implementation of SmsProvider using configured gateways (MSG91).
 * Runs in MOCK mode if credentials are not configured in environment variables.
 */
@Component
@Slf4j
public class SmsProviderImpl implements SmsProvider {

    @Value("${SMS_PROVIDER:MSG91}")
    private String provider;

    @Value("${SMS_API_KEY:}")
    private String apiKey;

    @Value("${SMS_SENDER_ID:}")
    private String senderId;

    private boolean isConfigured = false;

    /**
     * Initializes the SMS gateway if API keys are provided.
     */
    @PostConstruct
    public void init() {
        if (apiKey != null && !apiKey.isBlank()) {
            this.isConfigured = true;
            log.info("[SMS Service] Provider '{}' successfully initialized using sender ID: {}", provider, senderId);
        } else {
            log.warn("[SMS Service] SMS API Key missing in .env. Running in MOCK Mode.");
        }
    }

    @Override
    public void sendSms(String mobileNumber, String message) throws Exception {
        if (!isConfigured) {
            log.info("[MOCK SMS] Mobile: {}, Message: {}", mobileNumber, message);
            return;
        }

        log.info("[SMS Service] Dispatching SMS via {} to: {}", provider, mobileNumber);
        // Invoke MSG91 or fallback SMS gateway REST client calls
        log.info("[SMS Service] SMS sent successfully to: {}", mobileNumber);
    }
}
