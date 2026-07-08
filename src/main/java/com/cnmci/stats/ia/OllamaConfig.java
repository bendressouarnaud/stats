package com.cnmci.stats.ia;

import dev.langchain4j.model.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class OllamaConfig {

    @Value("${ollama.base-url}") private String baseUrl;
    @Value("${ollama.model}")    private String model;
    @Value("${ollama.timeout}") private int timeoutSeconds;


    @Bean
    public OllamaChatModel ollamaChatModel() {
        return OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(model)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .temperature(0.0)   // deterministic - critical for SQL
                .build();
    }
}
