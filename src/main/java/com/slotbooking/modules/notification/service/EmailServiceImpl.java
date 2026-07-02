package com.slotbooking.modules.notification.service;

import com.slotbooking.modules.notification.provider.EmailProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service implementation for EmailService.
 * Dispatches SMTP emails asynchronously.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final EmailProvider emailProvider;
    private final TemplateService templateService;

    @Async
    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            log.info("[Email Service] Dispatched email async to: {}", to);
            emailProvider.sendEmail(to, subject, body);
            log.info("[Email Service] Email Sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("[Email Service] Email Failed to send to {}: {}", to, e.getMessage());
        }
    }

    @Async
    @Override
    public void sendTemplateEmail(String to, String templateName, Map<String, Object> variables) {
        try {
            log.info("[Email Service] Processing template email '{}' for recipient: {}", templateName, to);
            String renderedBody = templateService.renderTemplate(templateName, variables);
            String subject = templateService.renderSubject(templateName, variables);
            
            emailProvider.sendEmail(to, subject, renderedBody);
            log.info("[Email Service] Email Sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("[Email Service] Email Failed to send template '{}' to {}: {}", templateName, to, e.getMessage());
        }
    }
}
