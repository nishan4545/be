package com.slotbooking.modules.notification.service;

import java.util.Map;

/**
 * Service interface for SMTP email operations.
 */
public interface EmailService {

    /**
     * Sends a raw email message.
     *
     * @param to      recipient email address
     * @param subject email subject line
     * @param body    email body html content
     */
    void sendEmail(String to, String subject, String body);

    /**
     * Sends an email populated from a database template.
     *
     * @param to           recipient email address
     * @param templateName database template name
     * @param variables    placeholder keys and values
     */
    void sendTemplateEmail(String to, String templateName, Map<String, Object> variables);
}
