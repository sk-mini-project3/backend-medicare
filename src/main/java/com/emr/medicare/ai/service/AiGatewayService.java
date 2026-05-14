package com.emr.medicare.ai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiGatewayService {

    private final WebClient.Builder webClientBuilder;

    @Value("${ai.base-url}")
    private String aiBaseUrl;

    public Map<String, Object> fetchAlerts(boolean useRds) {
        return webClientBuilder
                .baseUrl(aiBaseUrl)
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/ai/alerts")
                        .queryParam("use_rds", useRds)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public Map<String, Object> analyzeUser(Long userId, boolean useRds) {
        return webClientBuilder
                .baseUrl(aiBaseUrl)
                .build()
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/ai/analyze")
                        .queryParam("use_rds", useRds)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("user_id", userId))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}
