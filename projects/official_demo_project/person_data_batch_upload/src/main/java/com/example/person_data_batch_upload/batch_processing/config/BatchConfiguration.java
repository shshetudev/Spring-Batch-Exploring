package com.example.person_data_batch_upload.batch_processing.config;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchConfiguration {
    @Bean
    public ExecutionContext executionContext() {
        return new ExecutionContext();
    }
}
