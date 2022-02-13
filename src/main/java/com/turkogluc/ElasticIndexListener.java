package com.turkogluc;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import io.micronaut.http.annotation.Get;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Singleton
public class ElasticIndexListener {

    Logger logger = LoggerFactory.getLogger(ElasticIndexListener.class);

    private final ElasticsearchClient client;
    private final MailTemplateProcessor mailTemplateProcessor;
    private final ElasticConfig elasticConfig;
    private final EmailService emailService;

    public ElasticIndexListener(MailTemplateProcessor mailTemplateProcessor, ElasticConfig elasticConfig, EmailService emailService) {
        this.mailTemplateProcessor = mailTemplateProcessor;
        this.elasticConfig = elasticConfig;
        this.emailService = emailService;

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(elasticConfig.username, elasticConfig.password));


        RestClient restClient = RestClient.builder(new HttpHost(elasticConfig.host, elasticConfig.port))
                .setHttpClientConfigCallback((httpClientBuilder) -> httpClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider)).build();

        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        this.client = new ElasticsearchClient(transport);
    }

    @Scheduled(fixedDelay = "1m")
    public void read() throws IOException {
        SearchResponse <Map> result = client.search(s -> s
                        .index(elasticConfig.alertIndexName)
                        .sort(sort -> sort.field(f -> f.field("date").order(SortOrder.Asc)))
                        .from(0).size(1000)
                        .query(q -> q
                                .bool(b -> b
                                        .mustNot(q2 -> q2
                                                .exists(e -> e.field("processed"))))),
                Map.class);

        logger.info("{} new alerts received.", result.hits().hits().size());
        for (Hit <Map> hit : result.hits().hits()) {
            String id = hit.id();
            Map source = hit.source();
            logger.info(hit.source().toString());

            Optional <String> htmlBody = mailTemplateProcessor.process(source);
            if (htmlBody.isPresent()) {
                boolean success = emailService.send(htmlBody.get());
                if (success) {
                    updateDocumentAsProcessed(id);
                }
            }
        }
    }

    public void updateDocumentAsProcessed(String id) throws IOException {
        logger.info("Setting doc as processed. (id:{})", id);
        client.update(new UpdateRequest.Builder()
                .id(id)
                .doc(Map.of("processed", true))
                .index("alert-log")
                .build(), Map.class);
    }
}
