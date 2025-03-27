package com.vibevault.productservice.configurations;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationConfig {
    @Bean
    public RestTemplate restTemplate() {
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        return restTemplateBuilder.build();
    }
}
