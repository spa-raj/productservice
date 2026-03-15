package com.vibevault.productservice.configurations;

import co.elastic.clients.transport.rest5_client.Rest5ClientOptions;
import co.elastic.clients.transport.rest5_client.low_level.Rest5ClientBuilder;
import co.elastic.clients.transport.rest5_client.low_level.RequestOptions;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.springframework.boot.elasticsearch.autoconfigure.Rest5ClientBuilderCustomizer;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// ES 9.x Java client is incompatible with AWS OpenSearch 2.x in two ways:
// 1. Sends versioned content-type "application/vnd.elasticsearch+json; compatible-with=9"
// 2. Expects "X-Elastic-Product: Elasticsearch" response header
// This config fixes both by overriding request headers and injecting the product header on responses.
@Configuration
public class ElasticsearchConfig {

    @Bean
    Rest5ClientOptions rest5ClientOptions() {
        RequestOptions requestOptions = RequestOptions.DEFAULT.toBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();
        return new Rest5ClientOptions(requestOptions, false);
    }

    @Bean
    Rest5ClientBuilderCustomizer opensearchCompatibilityCustomizer() {
        return new Rest5ClientBuilderCustomizer() {
            @Override
            public void customize(Rest5ClientBuilder builder) {
                // no-op — required by interface
            }

            @Override
            public void customize(HttpAsyncClientBuilder builder) {
                builder.addResponseInterceptorFirst((HttpResponse response, org.apache.hc.core5.http.EntityDetails entity, HttpContext context) -> {
                    response.setHeader("X-Elastic-Product", "Elasticsearch");
                });
            }
        };
    }
}
