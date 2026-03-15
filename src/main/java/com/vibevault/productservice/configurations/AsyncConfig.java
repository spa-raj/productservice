package com.vibevault.productservice.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

// TODO: Configure a bounded ThreadPoolTaskExecutor to prevent unbounded thread creation
// under high write volume. Will be replaced by Kafka consumer when Kafka is integrated.
@Configuration
@EnableAsync
public class AsyncConfig {
}
