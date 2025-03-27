package com.vibevault.productservice.configurations;

import com.vibevault.productservice.services.ProductService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ProductServiceConfig {

    @Bean
    @Primary
    public ProductService productService(ApplicationContext context,
                                         @Value("${productservicetype}") String serviceType) {
        return context.getBean(serviceType, ProductService.class);
    }
}
