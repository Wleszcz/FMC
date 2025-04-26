package com.FMC.FMC.clients;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

@Service
public class OpenRouteServiceClient {

    private final WebClient webClient;

    @Value("${openrouteservice.api.key}")
    private String apiKey;

    @Value("${openrouteservice.api.url}")
    private String apiUrl;

    public OpenRouteServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
    }

    public Mono<Map<String, Object>> getTravelTime(double startLat, double startLon, double endLat, double endLon) {
        String requestUrl = String.format("%s?api_key=%s&start=%f,%f&end=%f,%f",
                apiUrl, apiKey, startLon, startLat, endLon, endLat);

        return webClient.get()
                .uri(requestUrl)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {}).retryWhen(Retry.backoff(3, Duration.ofSeconds(3))
                        .filter(throwable -> throwable instanceof WebClientResponseException.TooManyRequests));
    }
}
