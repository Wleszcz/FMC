package com.FMC.FMC.service;


import com.FMC.FMC.Place;
import com.FMC.FMC.clients.OpenRouteServiceClient;
import com.FMC.FMC.clients.OsrmClient;
import com.FMC.FMC.heatMap.Coord;
import com.FMC.FMC.heatMap.FileRepository.PlaceStorage;
import com.FMC.FMC.heatMap.HeatPoint;
import com.FMC.FMC.heatMap.SavedPlace;
import com.FMC.FMC.utils.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

import static com.FMC.FMC.utils.ControllerHelper.extractTravelSummary;
import static com.FMC.FMC.utils.ControllerHelper.generateCoords;
import static com.FMC.FMC.utils.GeoUtils.calculateIndex;

@Service
@RequiredArgsConstructor
public class DefaultHeatService implements HeatService {
    private final OsrmClient orsClient;
    private final PlaceStorage placeStorage;

    public static final double START_LAT = 54.42747;
    public static final double END_LAT = 54.34584;
    public static final double START_LON = 18.53531;
    public static final double END_LON = 18.70863;

    private static final double LAT_STEP = 0.00038; // ~40m
    private static final double LON_STEP = 0.00038; // ~40m

    @Override
    public List<HeatPoint> calculateHeatPoints() {
        List<Coord> coords = generateCoords(START_LAT, END_LAT, START_LON, END_LON, LAT_STEP, LON_STEP);
        Map<Place, Set<SavedPlace>> placesByType = loadAllPlaces();
        final int[] i = {0};
        return coords.stream()
                .map(coord -> {
                    List<Double> distances = placesByType.values().stream()
                            .map(set -> findClosestDistanceAPI(coord, set))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    int score = calculateIndex(distances);
                    i[0]++;
                    System.out.println(i[0] + "/" + coords.size());
                    return new HeatPoint(coord.getLat(), coord.getLon(), score);
                })
                .toList();
    }

    private Map<Place, Set<SavedPlace>> loadAllPlaces() {
        Map<Place, Set<SavedPlace>> map = new HashMap<>();
        Arrays.stream(Place.values()).forEach(p -> map.put(p, placeStorage.load(p)));
        return map;
    }

    private Double findClosestDistance(Coord coord, Set<SavedPlace> places) {
        return places.stream()
                .map(p -> GeoUtils.jtsDistanceInMeters(coord.getLat(), coord.getLon(), p.getLat(), p.getLon()))
                .min(Double::compareTo)
                .orElse(null);
    }
    private Double findClosestDistanceAPI(Coord coord, Set<SavedPlace> places) {
        return (Double) places.stream()
                .min(Comparator.comparing(p -> GeoUtils.jtsDistanceInMeters(coord.getLat(), coord.getLon(), p.getLat(), p.getLon())))
                .map(p -> {
                    try {
                        return orsClient.getTravelTime(coord.getLat(), coord.getLon(), p.getLat(), p.getLon())
                                .flatMap(response -> {
                                    if (response == null || response.get("routes") == null) {
                                        return Mono.empty();
                                    }
                                    List<?> routes = (List<?>) response.get("routes");
                                    if (routes.isEmpty() || routes.get(0) == null) {
                                        return Mono.empty();
                                    }
                                    Map<String, Object> route = (Map<String, Object>) routes.get(0);
                                    Object durationObj = route.get("duration");
                                    if (durationObj instanceof Number number) {
                                        // 👇 zamiast rzutowania
                                        return Mono.just(number.doubleValue());
                                    } else {
                                        return Mono.empty();
                                    }
                                })
                                .onErrorResume(e -> {
                                    System.out.println("Błąd pobierania czasu przejazdu: " + e.getMessage());
                                    return Mono.empty();
                                })
                                .block();
                    } catch (Exception e) {
                        System.out.println("Exception przy pobieraniu czasu: " + e.getMessage());
                        return 9999; // lub np. 9999.0
                    }
                })
                .orElse(null);
    }

}

