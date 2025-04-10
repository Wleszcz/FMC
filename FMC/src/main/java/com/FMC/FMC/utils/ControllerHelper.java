package com.FMC.FMC.utils;

import com.FMC.FMC.Place;
import com.FMC.FMC.heatMap.Coord;
import com.FMC.FMC.heatMap.SavedPlace;

import java.util.*;

public class ControllerHelper {

    public static Map<String, Object> extractTravelSummary(Map<String, Object> travelData) {
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

    public static SavedPlace mapToSavedPlace(Place place, Map<String, Object> element) {
        Optional<double[]> coordOpt = extractCoordinates(element);
        if (coordOpt.isEmpty()) {
            return null;
        }

        double[] coords = coordOpt.get();
        Number rawId = (Number) element.get("id");
        Long id = rawId != null ? rawId.longValue() : null;


        String name = "Unknown";
        if (element.containsKey("tags")) {
            Object tagsObj = element.get("tags");
            if (tagsObj instanceof Map<?, ?> tagsMap) {
                Object nameValue = tagsMap.get("name");
                if (nameValue instanceof String s) {
                    name = s;
                }
            }
        }

        return SavedPlace.builder()
                .id(id)
                .name(name)
                .lat(coords[0])
                .lon(coords[1])
                .place(place)
                .build();
    }


    public static Optional<double[]> extractCoordinates(Map<String, Object> element) {
        Number latNum = (Number) element.get("lat");
        Number lonNum = (Number) element.get("lon");

        if (latNum == null || lonNum == null) {
            Map<String, Object> center = (Map<String, Object>) element.get("center");
            if (center != null) {
                latNum = (Number) center.get("lat");
                lonNum = (Number) center.get("lon");
            }
        }

        if (latNum != null && lonNum != null) {
            return Optional.of(new double[]{latNum.doubleValue(), lonNum.doubleValue()});
        }

        return Optional.empty();
    }

    public static List<Coord> generateCoords(double startLat, double endLat, double startLon, double endLon, double latStep, double lonStep) {
        List<Coord> coords = new ArrayList<>();
        for (double lat = startLat; lat >= endLat; lat -= latStep ) {
            for (double lon = startLon; lon <= endLon; lon += lonStep) {
                coords.add(new Coord(lat, lon));
            }
        }
        return coords;
    }

}
