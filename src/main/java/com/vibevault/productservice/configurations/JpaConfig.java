package com.vibevault.productservice.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // This class is used to enable JPA auditing features
    // such as @CreatedDate and @LastModifiedDate annotations.

}
