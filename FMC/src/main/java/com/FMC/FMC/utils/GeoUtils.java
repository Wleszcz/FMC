package com.FMC.FMC.utils;

import com.FMC.FMC.Place;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.operation.distance.DistanceOp;

import java.util.List;

public class GeoUtils {
    private static final double MAX_DISTANCE_M = 4000;


    public static double jtsDistance(double lat1, double lon1, double lat2, double lon2) {
        GeometryFactory geometryFactory = new GeometryFactory();
        Point point1 = geometryFactory.createPoint(new Coordinate(lon1, lat1));
        Point point2 = geometryFactory.createPoint(new Coordinate(lon2, lat2));
        return DistanceOp.distance(point1, point2);
    }

    public static double jtsDistanceInMeters(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371000; // Promień Ziemi w metrach

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    public static int calculateIndex(List<Double> distancesM) {
        if (distancesM.isEmpty()) return 0;

        double total = distancesM.stream()
                .mapToDouble(d -> {
                    if (d > MAX_DISTANCE_M) {
                        return 0;
                    }
//                    else {
//                        return (50 + (1 - (d / MAX_DISTANCE_M)) * 50)
//                    }
                    return (1 - (d / MAX_DISTANCE_M)) * 100 / Place.values().length;
                }).sum();

        return Math.min((int) total, 100);
    }
}
