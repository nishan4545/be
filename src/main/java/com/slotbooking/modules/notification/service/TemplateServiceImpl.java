package com.slotbooking.modules.notification.service;

import com.slotbooking.modules.notification.entity.NotificationTemplate;
import com.slotbooking.modules.notification.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service implementation for TemplateService.
 * Compiles database template patterns replacing variable parameters.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateServiceImpl implements TemplateService {

    private final NotificationTemplateRepository templateRepository;

    @Override
    public String renderTemplate(String templateName, Map<String, Object> variables) {
        NotificationTemplate template = templateRepository.findByTemplateName(templateName).orElse(null);
        String body = template != null ? template.getBody() : getDefaultBody(templateName);
        return replaceVariables(body, variables);
    }

    @Override
    public String renderSubject(String templateName, Map<String, Object> variables) {
        NotificationTemplate template = templateRepository.findByTemplateName(templateName).orElse(null);
        String subject = template != null ? template.getSubject() : getDefaultSubject(templateName);
        return replaceVariables(subject, variables);
    }

    /**
     * Replaces placeholders formatted as {{variableName}} with values.
     */
    private String replaceVariables(String text, Map<String, Object> variables) {
        if (text == null || variables == null) {
            return text;
        }
        String result = text;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }

    private String getDefaultSubject(String templateName) {
        return "Notification Alert: " + templateName.replace("_", " ");
    }

    private String getDefaultBody(String templateName) {
        return "System notification for event: " + templateName.replace("_", " ") 
                + ". Details: {{message}}{{tournamentTitle}}{{bookingId}}{{amount}}";
    }
}
