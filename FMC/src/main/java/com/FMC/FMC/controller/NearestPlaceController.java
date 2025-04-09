package com.FMC.FMC.controller;

import com.FMC.FMC.Place;
import com.FMC.FMC.clients.OpenRouteServiceClient;
import com.FMC.FMC.clients.OverpassClient;
import com.FMC.FMC.heatMap.SavedPlace;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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

import static com.FMC.FMC.utils.ControllerHelper.extractTravelSummary;

@RestController
@RequestMapping("/api")
public class NearestPlaceController {

    private final OverpassClient overpassClient;
    private final OpenRouteServiceClient orsClient;
    private final MessageSource messageSource;

    public NearestPlaceController(OverpassClient overpassClient, OpenRouteServiceClient orsClient, MessageSource messageSource) {
        this.overpassClient = overpassClient;
        this.orsClient = orsClient;
        this.messageSource = messageSource;
    }

    @GetMapping("/nearest-place")
    public Mono<Map<String, Object>> getNearestPlace(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam String type) {

        return overpassClient.findNearestPlace(lat, lon, Place.getPlaceFromName(type))
                .flatMap(place -> {
                    return orsClient.getTravelTime(lat, lon, place.getLat(), place.getLon())
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
    public Mono<List<Map<String, Object>>> getAmenityList(@RequestParam double lat, @RequestParam double lon) {
        return Flux.fromStream(Arrays.stream(Place.values()))
                .flatMap(type -> overpassClient.findNearestPlace(lat, lon, type)
                        .delayElement(Duration.ofSeconds(1))
                        .retryWhen(Retry.fixedDelay(5, Duration.ofSeconds(2)))
                        .flatMap(place -> {
                            return orsClient.getTravelTime(lat, lon, place.getLat(), place.getLon())
                                    .map(travelData -> {
                                        Map<String, Object> response = new HashMap<>();
                                        response.put("displayType", messageSource.getMessage("amenity." + type.name(), null, LocaleContextHolder.getLocale()));
                                        response.put("type", type.name().toLowerCase());
                                        response.put("place", place);
                                        response.put("travel", extractTravelSummary(travelData));
                                        return response;
                                    });
                        })
                        .defaultIfEmpty(Map.of("displayType", messageSource.getMessage("amenity." + type.name(), null, LocaleContextHolder.getLocale()),
                                "type", type.name().toLowerCase())))
                .collectList();
    }

    @GetMapping("/all-places")
    public  Mono<List<SavedPlace>> getAllPlaces(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam String type,
            @RequestParam Long radius) {

        return overpassClient.findAllPlaces(lat, lon, Place.getPlaceFromName(type), radius);
    }
}
