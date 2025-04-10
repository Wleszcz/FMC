package com.FMC.FMC.heatMap.FileRepository;

import com.FMC.FMC.Place;
import com.FMC.FMC.heatMap.SavedPlace;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

@Component
public class DefaultFilePlaceStorage implements PlaceStorage {

    private static final String FILE_PATH = "places/";

    @Override
    public Set<SavedPlace> load(Place place) {
        File file = new File(FILE_PATH + place.name() + ".ser");
        if (!file.exists()) return new HashSet<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Set<SavedPlace>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return new HashSet<>();
        }
    }

    @Override
    public void save(Set<SavedPlace> places, Place place) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH + place.name() + ".ser"))) {
            oos.writeObject(places);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
