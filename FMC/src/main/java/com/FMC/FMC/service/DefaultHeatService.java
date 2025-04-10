package com.FMC.FMC.service;


import com.FMC.FMC.Place;
import com.FMC.FMC.heatMap.Coord;
import com.FMC.FMC.heatMap.FileRepository.PlaceStorage;
import com.FMC.FMC.heatMap.HeatPoint;
import com.FMC.FMC.heatMap.SavedPlace;
import com.FMC.FMC.utils.ControllerHelper;
import com.FMC.FMC.utils.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.FMC.FMC.utils.ControllerHelper.generateCoords;
import static com.FMC.FMC.utils.GeoUtils.calculateIndex;

@Service
@RequiredArgsConstructor
public class DefaultHeatService implements HeatService {

    private final PlaceStorage placeStorage;

    public static final double START_LAT = 54.42747;
    public static final double END_LAT = 54.28747;
    public static final double START_LON = 18.53531;
    public static final double END_LON = 18.67531;

    private static final double LAT_STEP = 0.00019; // ~10m
    private static final double LON_STEP = 0.00030; // ~10m


    @Override
    public List<HeatPoint> calculateHeatPoints() {
        List<Coord> coords = generateCoords(START_LAT, END_LAT, START_LON, END_LON, LAT_STEP, LON_STEP);
        Map<Place, Set<SavedPlace>> placesByType = loadAllPlaces();

        return coords.stream()
                .map(coord -> {
                    List<Double> distances = placesByType.values().stream()
                            .map(set -> findClosestDistance(coord, set))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    int score = calculateIndex(distances);
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
}
