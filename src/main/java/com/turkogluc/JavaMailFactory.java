package com.turkogluc;

import io.micronaut.context.annotation.Factory;
import io.micronaut.email.javamail.sender.MailPropertiesProvider;
import io.micronaut.email.javamail.sender.SessionProvider;
import jakarta.inject.Singleton;

import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.util.Properties;

@Factory
public class JavaMailFactory {

    private final EmailConfig emailConfig;

    public JavaMailFactory(EmailConfig emailConfig) {
        this.emailConfig = emailConfig;
    }

    @Singleton
    public MailPropertiesProvider mailPropertiesProvider() {
        return () -> {
            Properties properties = new Properties();
            properties.put("mail.smtp.host", emailConfig.host);
            properties.put("mail.smtp.port", emailConfig.port);
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.auth", "true");
            return properties;
        };
    }

    @Singleton
    public SessionProvider sessionProvider() {
        return () -> {
            Properties properties = mailPropertiesProvider().mailProperties();
            return Session.getInstance(properties, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(emailConfig.username, emailConfig.password);
                }
            });
        };
    }
}
