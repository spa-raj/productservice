package com.vibevault.productservice.configurations;

import co.elastic.clients.transport.rest5_client.Rest5ClientOptions;
import co.elastic.clients.transport.rest5_client.low_level.RequestOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Override default ES 9.x versioned content-type headers for AWS OpenSearch 2.x compatibility.
// OpenSearch rejects "application/vnd.elasticsearch+json; compatible-with=9".
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
}
