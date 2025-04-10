package com.FMC.FMC.clients;

import com.FMC.FMC.Place;
import com.FMC.FMC.heatMap.SavedPlace;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

import static com.FMC.FMC.utils.ControllerHelper.mapToSavedPlace;
import static com.FMC.FMC.utils.GeoUtils.jtsDistanceInMeters;

@Service
public class OverpassClient {

    private static final Long DEFAULT_RADIUS = 4000L;
    @Value("${overpass.api.url}")
    String apiUrl;

    private final WebClient webClient;

    public OverpassClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(this.apiUrl).build();
    }

    public Mono<List<Map<String, Object>>> fetchRawPlaces(double lat, double lon, Place place, Long radius) {
        String key = place.getPlaceType().name().toLowerCase();
        String value = place.name().toLowerCase();

        String query = String.format("""
        [out:json];
        (
          node["%s"="%s"](around:%d,%.6f,%.6f);
          way["%s"="%s"](around:%d,%.6f,%.6f);
          relation["%s"="%s"](around:%d,%.6f,%.6f);
        );
        out center 100;
        """,
                key, value, radius, lat, lon,
                key, value, radius, lat, lon,
                key, value, radius, lat, lon
        );

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("data", query);

        return webClient.post()
                .uri(apiUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (List<Map<String, Object>>) response.getOrDefault("elements", Collections.emptyList()));
    }


    public Mono<SavedPlace> findNearestPlace(double lat, double lon, Place place) {
        return findAllPlaces(lat, lon, place, DEFAULT_RADIUS)
                .flatMap(places -> {
                    if (places.isEmpty()) {
                        return Mono.empty();
                    }

                    places.sort(Comparator.comparingDouble(p ->
                            jtsDistanceInMeters(lat, lon, p.getLat(), p.getLon()))
                    );

                    return Mono.just(places.getFirst());
                });
    }

    public Mono<List<SavedPlace>> findAllPlaces(double lat, double lon, Place place, Long radius) {
        return fetchRawPlaces(lat, lon, place, radius)
                .map(elements -> elements.stream()
                        .map(e -> mapToSavedPlace(place, e))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
                );
    }
}
