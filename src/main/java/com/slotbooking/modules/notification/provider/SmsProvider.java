package com.slotbooking.modules.notification.provider;

/**
 * Interface mapping external SMS delivery gateway operations.
 */
public interface SmsProvider {

    /**
     * Sends an SMS alert to the target mobile number.
     *
     * @param mobileNumber recipient mobile number
     * @param message      message text content
     * @throws Exception if delivery fails
     */
    void sendSms(String mobileNumber, String message) throws Exception;
}
