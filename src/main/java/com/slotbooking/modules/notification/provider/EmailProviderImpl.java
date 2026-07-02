package com.slotbooking.modules.notification.provider;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Properties;

/**
 * Implementation of EmailProvider using SMTP configurations.
 * Runs in MOCK mode if credentials are not configured in environment variables.
 */
@Component
@Slf4j
public class EmailProviderImpl implements EmailProvider {

    @Value("${SMTP_HOST:}")
    private String host;

    @Value("${SMTP_PORT:587}")
    private int port;

    @Value("${SMTP_USERNAME:}")
    private String username;

    @Value("${SMTP_PASSWORD:}")
    private String password;

    private JavaMailSender mailSender;
    private boolean isConfigured = false;

    /**
     * Initializes the SMTP mail sender if environment variables are provided.
     */
    @PostConstruct
    public void init() {
        if (host != null && !host.isBlank() && username != null && !username.isBlank()) {
            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            sender.setHost(host);
            sender.setPort(port);
            sender.setUsername(username);
            sender.setPassword(password);

            Properties props = sender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.debug", "false");

            this.mailSender = sender;
            this.isConfigured = true;
            log.info("[Email Service] SMTP Mail Sender successfully initialized for host: {}", host);
        } else {
            log.warn("[Email Service] SMTP credentials missing in .env. Running in MOCK Mode.");
        }
    }

    @Override
    public void sendEmail(String to, String subject, String body) throws Exception {
        if (!isConfigured) {
            log.info("[MOCK Email] To: {}, Subject: {}, Body: {}", to, subject, body);
            return;
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);
        helper.setFrom(username);

        log.info("[Email Service] Dispatching SMTP email to: {}", to);
        mailSender.send(message);
        log.info("[Email Service] Email sent successfully to: {}", to);
    }
}
