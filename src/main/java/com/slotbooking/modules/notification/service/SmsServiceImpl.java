package com.slotbooking.modules.notification.service;

import com.slotbooking.modules.notification.provider.SmsProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service implementation for SmsService.
 * Coordinates SMS dispatches and alerts asynchronously.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SmsServiceImpl implements SmsService {

    private final SmsProvider smsProvider;

    @Async
    @Override
    public void sendSms(String mobileNumber, String message) {
        try {
            log.info("[SMS Service] Sending SMS async to: {}", mobileNumber);
            smsProvider.sendSms(mobileNumber, message);
            log.info("[SMS Service] SMS Sent successfully to: {}", mobileNumber);
        } catch (Exception e) {
            log.error("[SMS Service] SMS Failed to send to {}: {}", mobileNumber, e.getMessage());
        }
    }

    @Async
    @Override
    public void sendOtp(String mobileNumber, String otp) {
        String msg = "Your verification OTP code is: " + otp + ". Valid for 5 minutes. Do not share it.";
        sendSms(mobileNumber, msg);
    }

    @Async
    @Override
    public void sendTournamentReminder(String mobileNumber, String tournamentTitle, String dateStr) {
        String msg = "Reminder: Your tournament '" + tournamentTitle + "' starts on " + dateStr + ". Prepare your match!";
        sendSms(mobileNumber, msg);
    }
}
