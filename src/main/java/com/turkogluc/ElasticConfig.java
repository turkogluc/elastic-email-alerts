package com.turkogluc;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("elastic")
public class ElasticConfig {
    public String username;
    public String password;
    public String host;
    public int port;
    public String alertIndexName;
}
