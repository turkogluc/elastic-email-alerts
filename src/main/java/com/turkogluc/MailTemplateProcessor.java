package com.turkogluc;

import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class MailTemplateProcessor {

    private final Logger logger = LoggerFactory.getLogger(MailTemplateProcessor.class);
    private final TemplateEngine templateEngine;
    private final ElasticConfig elasticConfig;

    public MailTemplateProcessor(ElasticConfig elasticConfig) {
        this.elasticConfig = elasticConfig;
        this.templateEngine = new TemplateEngine();
    }

    public Optional <String> process(Map values) {
        Context context = new Context();
        List <Item> items = new ArrayList <>();
        values.forEach((title, value) -> {
            items.add(new Item(String.valueOf(title), String.valueOf(value)));
            if (String.valueOf(title).equals("contextReason")) {
                context.setVariable("message", String.valueOf(value));
            }
            context.setVariable("elastic_url", elasticConfig.host + ":" + elasticConfig.port);
        });
        context.setVariable("items", items);

        try {
            return Optional.of(templateEngine.process(getTemplate(), context));
        } catch (IOException e) {
            logger.error("Error while processing email template", e);
            return Optional.empty();
        }

    }

    private String getTemplate() throws IOException {
        return new String(getClass().getClassLoader()
                .getResourceAsStream("templates/mail.html").readAllBytes());
    }

    public static class Item implements Serializable {
        public String title;
        public String value;

        public Item(String title, String value) {
            this.title = title;
            this.value = value;
        }
    }
}
