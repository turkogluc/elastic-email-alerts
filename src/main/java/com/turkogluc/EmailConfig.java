package com.turkogluc;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("email")
public class EmailConfig {
    public String host;
    public String port;
    public String username;
    public String password;
    public String to;
    public String subject;
    public String cc;
}
