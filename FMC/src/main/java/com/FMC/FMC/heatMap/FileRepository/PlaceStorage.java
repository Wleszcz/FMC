package com.FMC.FMC.heatMap.FileRepository;

import com.FMC.FMC.Place;
import com.FMC.FMC.heatMap.SavedPlace;

import java.util.Set;

public interface PlaceStorage {
    Set<SavedPlace> load(Place place);

    void save(Set<SavedPlace> places, Place place);
}


