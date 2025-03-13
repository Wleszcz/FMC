package com.FMC.FMC.controller;

import com.FMC.FMC.Amenity;
import com.FMC.FMC.clients.OpenRouteServiceClient;
import com.FMC.FMC.clients.OverpassClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class NearestPlaceController {

    private final OverpassClient overpassClient;
    private final OpenRouteServiceClient orsClient;

    public NearestPlaceController(OverpassClient overpassClient, OpenRouteServiceClient orsClient) {
        this.overpassClient = overpassClient;
        this.orsClient = orsClient;
    }

    @GetMapping("/nearest-place")
    public Mono<Map<String, Object>> getNearestPlace(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam String type) {

        return overpassClient.findNearestPlace(lat, lon, type)
                .flatMap(place -> {
                    double placeLat = (double) place.get("lat");
                    double placeLon = (double) place.get("lon");

                    return orsClient.getTravelTime(lat, lon, placeLat, placeLon)
                            .map(travelData -> {
                                Map<String, Object> response = new HashMap<>();
                                response.put("place", place);
                                response.put("travel", extractTravelSummary(travelData));
//                                response.put("travel", travelData);
                                return response;
                            });
                });
    }

    @GetMapping("/get-amenity-list")
    public Mono<List<Map<String, Object>>> getIndex(@RequestParam double lat, @RequestParam double lon) {
        return Flux.fromStream(Arrays.stream(Amenity.values()))
                .flatMap(t -> overpassClient.findNearestPlace(lat, lon, t.name().toLowerCase())
                        .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1)))
                        .flatMap(place -> {
                            double placeLat = (double) place.get("lat");
                            double placeLon = (double) place.get("lon");

                            return orsClient.getTravelTime(lat, lon, placeLat, placeLon)
                                    .map(travelData -> {
                                        Map<String, Object> response = new HashMap<>();
                                        response.put("type", t.name().toLowerCase());
                                        response.put("place", place);
                                        response.put("travel", extractTravelSummary(travelData));
                                        return response;
                                    });
                        }))
                .collectList();
    }

    private Map<String, Object> extractTravelSummary(Map<String, Object> travelData) {
        Map<String, Object> travelSummary = new HashMap<>();
        if (travelData != null) {
            List<Map<String, Object>> features = (List<Map<String, Object>>) travelData.get("features");
            if (features != null && !features.isEmpty()) {
                Map<String, Object> firstFeature = features.get(0);
                Map<String, Object> properties = (Map<String, Object>) firstFeature.get("properties");
                if (properties != null) {
                    List<Map<String, Object>> segments = (List<Map<String, Object>>) properties.get("segments");
                    if (segments != null && !segments.isEmpty()) {
                        // Pierwszy segment zawiera interesujące nas dane
                        Map<String, Object> firstSegment = segments.get(0);
                        travelSummary.put("distance", firstSegment.get("distance"));
                        travelSummary.put("duration", firstSegment.get("duration"));
                    }
                }
            }
        }
        return travelSummary;
    }
}
