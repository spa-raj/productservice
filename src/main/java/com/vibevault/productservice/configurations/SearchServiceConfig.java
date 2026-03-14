package com.vibevault.productservice.configurations;

import com.vibevault.productservice.services.SearchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SearchServiceConfig {

    @Bean
    @Primary
    public SearchService searchService(ApplicationContext context,
                                       @Value("${searchServiceType}") String serviceType) {
        return context.getBean(serviceType, SearchService.class);
    }
}
