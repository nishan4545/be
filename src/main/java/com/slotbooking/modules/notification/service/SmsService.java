package com.slotbooking.modules.notification.service;

/**
 * Service interface for SMS alerts and OTP routing.
 */
public interface SmsService {

    /**
     * Sends a general SMS alert.
     *
     * @param mobileNumber recipient mobile number
     * @param message      message text
     */
    void sendSms(String mobileNumber, String message);

    /**
     * Sends an OTP verification token.
     *
     * @param mobileNumber recipient mobile number
     * @param otp          one-time password token
     */
    void sendOtp(String mobileNumber, String otp);

    /**
     * Sends a tournament schedule reminder text alert.
     *
     * @param mobileNumber    recipient mobile number
     * @param tournamentTitle title of the tournament
     * @param dateStr         formatted start date and time
     */
    void sendTournamentReminder(String mobileNumber, String tournamentTitle, String dateStr);
}
