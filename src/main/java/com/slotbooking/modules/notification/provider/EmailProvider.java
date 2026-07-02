package com.slotbooking.modules.notification.provider;

/**
 * Interface mapping external email delivery provider operations.
 */
public interface EmailProvider {

    /**
     * Sends an email to the recipient.
     *
     * @param to      recipient email address
     * @param subject email subject line
     * @param body    email body (can contain HTML content)
     * @throws Exception if delivery fails
     */
    void sendEmail(String to, String subject, String body) throws Exception;
}
