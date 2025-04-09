package com.FMC.FMC;

import java.util.Arrays;

public enum Place {
    ATM(PlaceType.AMENITY),
    HOSPITAL(PlaceType.AMENITY),
    SCHOOL(PlaceType.AMENITY),
    RESTAURANT(PlaceType.AMENITY),
    PLACE_OF_WORSHIP(PlaceType.AMENITY),
    LIBRARY(PlaceType.AMENITY),
    SUPERMARKET(PlaceType.SHOP),
    CINEMA(PlaceType.AMENITY);

    private final PlaceType placeType;

    Place(PlaceType placeType) {
        this.placeType = placeType;
    }

    public PlaceType getPlaceType() {
        return placeType;
    }

    public static Place getPlaceFromName(String name) {
        return Arrays.stream(values())
                .filter(t -> t.name().toLowerCase().equals(name))
                .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}