package com.FMC.FMC.clients;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class OsrmClient {

    private final WebClient webClient;


//    private static final String BASE_URL = "https://router.project-osrm.org";
    private static final String BASE_URL = "http://localhost:5000";

    public OsrmClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
    }

    public Mono<Map<String, Object>> getTravelTime(double startLat, double startLon, double endLat, double endLon) {
        String requestUrl = String.format("/route/v1/driving/%f,%f;%f,%f?overview=false&alternatives=false&steps=false",
                startLon, startLat, endLon, endLat);

        return webClient.get()
                .uri(requestUrl)
                .retrieve()
                .onStatus(status -> status.value() == 400,
                        clientResponse -> {
                            System.out.println("Bad Request: points likely outside the map area or not connected.");
                            return Mono.error(new RuntimeException("Bad Request 400 - No route or out of bounds"));
                        }
                )
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

}
