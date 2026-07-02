package com.slotbooking.modules.notification.service;

import java.util.Map;

/**
 * Service interface for notification template processing and variable replacements.
 */
public interface TemplateService {

    /**
     * Renders a template body content by replacing variable placeholders.
     *
     * @param templateName the database template name identifier
     * @param variables    placeholder key-value parameters
     * @return rendered message content string
     */
    String renderTemplate(String templateName, Map<String, Object> variables);

    /**
     * Renders a template subject header by replacing variable placeholders.
     *
     * @param templateName the database template name identifier
     * @param variables    placeholder key-value parameters
     * @return rendered subject header string
     */
    String renderSubject(String templateName, Map<String, Object> variables);
}
