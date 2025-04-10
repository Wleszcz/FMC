package com.FMC.FMC.controller;

import com.FMC.FMC.Place;
import com.FMC.FMC.clients.OpenRouteServiceClient;
import com.FMC.FMC.clients.OverpassClient;
import com.FMC.FMC.heatMap.Coord;
import com.FMC.FMC.heatMap.FileRepository.PlaceStorage;
import com.FMC.FMC.heatMap.HeatPoint;
import com.FMC.FMC.heatMap.SavedPlace;
import com.FMC.FMC.service.DefaultHeatService;
import com.FMC.FMC.service.HeatService;
import com.FMC.FMC.utils.ControllerHelper;
import com.FMC.FMC.utils.GeoUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.FMC.FMC.service.DefaultHeatService.*;
import static com.FMC.FMC.utils.ControllerHelper.extractTravelSummary;
import static com.FMC.FMC.utils.ControllerHelper.generateCoords;



@RestController
@RequestMapping("/api")
public class NearestPlaceController {

    private final OverpassClient overpassClient;
    private final OpenRouteServiceClient orsClient;
    private final MessageSource messageSource;
    private final PlaceStorage placeStorage;
    private final HeatService heatService;

    private List<HeatPoint> heatPoints = null;

    public NearestPlaceController(OverpassClient overpassClient, OpenRouteServiceClient orsClient, MessageSource messageSource, PlaceStorage placeStorage, HeatService heatService) {
        this.overpassClient = overpassClient;
        this.orsClient = orsClient;
        this.messageSource = messageSource;
        this.placeStorage = placeStorage;
        this.heatService = heatService;
    }

    @GetMapping("/nearest-place")
    public Mono<SavedPlace> getNearestPlace(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam String type) {

        return overpassClient.findNearestPlace(lat, lon, Place.getPlaceFromName(type));
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

    @GetMapping("/all-places/save")
    public Mono<Set<SavedPlace>> getAndSavePlaces(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam String type,
            @RequestParam Long radius
    ) {
        Place place = Place.getPlaceFromName(type);

        return overpassClient.findAllPlaces(lat, lon, place, radius)
                .map(newPlaces -> {
                    Set<SavedPlace> savedSet = placeStorage.load(place);
                    savedSet.addAll(newPlaces); // merge bez duplikatów
                    placeStorage.save(savedSet, place);
                    return savedSet;
                });
    }

    @GetMapping("/scan-gdansk")
    public ResponseEntity<String> scanGdansk(@RequestParam String type) {
        Place place = Place.getPlaceFromName(type);

        double latStep = 0.018; // ≈ 1 km
        double lonStep = 0.030; // ≈ 1 km
        List<Coord> coords = generateCoords(START_LAT, END_LAT, START_LON, END_LON, latStep, lonStep);

        int totalPoints = coords.size();
        AtomicInteger counter = new AtomicInteger(0);
        Set<Long> seenIds = ConcurrentHashMap.newKeySet();

        //Start background task
        Mono.fromRunnable(() -> {
            Flux.fromIterable(coords)
                    .delayElements(Duration.ofSeconds(1))
                    .flatMap(coord ->
                            overpassClient.findAllPlaces(coord.getLat(), coord.getLon(), place, 2000L)
                                    .retry(3)
                                    .delayElement(Duration.ofMillis(1500))
                                    .map(list -> {
                                        int current = counter.incrementAndGet();
                                        if (current % 1 == 0 || current == totalPoints) {
                                            System.out.printf("Processed %d / %d (%.2f%%) %d places found\n", current, totalPoints, (100.0 * current) / totalPoints, seenIds.size());
                                        }
                                        return list.stream()
                                                .filter(sp -> seenIds.add(sp.getId()))
                                                .collect(Collectors.toList());
                                    })
                    )
                    .flatMap(Flux::fromIterable)
                    .collect(Collectors.toSet())
                    .doOnNext(set -> placeStorage.save(set, place))
                    .subscribe();
        }).subscribe();

        return ResponseEntity.ok("Scan started in background.");
    }

    @GetMapping("/heatmap")
    public ResponseEntity<List<HeatPoint>> getHeatmap() {
       if (this.heatPoints == null){
           this.heatPoints = heatService.calculateHeatPoints();
       }
        return ResponseEntity.ok(this.heatPoints);
    }
}
