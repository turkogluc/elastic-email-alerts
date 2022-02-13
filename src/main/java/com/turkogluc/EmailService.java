package com.turkogluc;

import io.micronaut.email.BodyType;
import io.micronaut.email.Email;
import io.micronaut.email.EmailSender;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@Singleton
public class EmailService {

    private final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final EmailSender <?, ?> emailSender;
    private final EmailConfig emailConfig;

    public EmailService(EmailSender <?, ?> emailSender, EmailConfig emailConfig) {
        this.emailSender = emailSender;
        this.emailConfig = emailConfig;
    }

    public boolean send(String body) {
        Email.Builder emailBuilder = Email.builder()
                .from(emailConfig.username)
                .to(emailConfig.to)
                .subject(emailConfig.subject)
                .body(body, BodyType.HTML);

        if (Objects.nonNull(emailConfig.cc) && !emailConfig.cc.isEmpty() && !emailConfig.cc.isBlank()) {
            String[] split = emailConfig.cc.split(",");
            for (String s : split) {
                emailBuilder.cc(s.trim());
            }
        }
        emailSender.send(emailBuilder);
        logger.info("Alert email sent.");
        return true;
    }
}
