package com.FMC.FMC.heatMap;

import com.FMC.FMC.Place;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SavedPlace implements Serializable {
    private Long id;
    private Place place;
    private double lon;
    private double lat;
    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SavedPlace)) return false;
        SavedPlace that = (SavedPlace) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
