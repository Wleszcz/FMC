package com.FMC.FMC.clients;

import com.FMC.FMC.Place;
import com.FMC.FMC.PlaceType;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.operation.distance.DistanceOp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class OverpassClient {

    @Value("${overpass.api.url}")
    String apiUrl;

    private final WebClient webClient;

    public OverpassClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(this.apiUrl).build();
    }

    public Mono<Map<String, Object>> findNearestPlace(double lat, double lon, Place place) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("[out:json];");
        queryBuilder.append("node");

        queryBuilder.append("[\"")
                .append(place.getPlaceType().name().toLowerCase())
                .append("\"=\"")
                .append(place.name().toLowerCase())
                .append("\"]");

        queryBuilder.append(String.format("(around:4000,%f,%f);", lat, lon));
        queryBuilder.append("out 50;");

        String fullUrl = apiUrl + "?data=" + queryBuilder.toString();

        try {
            return webClient.get()
                    .uri(fullUrl)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .flatMap(response -> {
                        List<Map<String, Object>> elements = (List<Map<String, Object>>) response.get("elements");
                        if (elements == null || elements.isEmpty()) {
                            return Mono.empty();
                        }
                        elements.sort(Comparator.comparingDouble(e -> {
                            double eLat = ((Number) e.get("lat")).doubleValue();
                            double eLon = ((Number) e.get("lon")).doubleValue();
                            return jtsDistance(lat, lon, eLat, eLon);
                        }));
                        return Mono.just(elements.getFirst());
                    });
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private double jtsDistance(double lat1, double lon1, double lat2, double lon2) {
        GeometryFactory geometryFactory = new GeometryFactory();
        Point point1 = geometryFactory.createPoint(new Coordinate(lon1, lat1));
        Point point2 = geometryFactory.createPoint(new Coordinate(lon2, lat2));
        return DistanceOp.distance(point1, point2);
    }
}
