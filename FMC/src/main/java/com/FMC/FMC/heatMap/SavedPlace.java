package com.FMC.FMC.heatMap;

import com.FMC.FMC.Place;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SavedPlace {
    private Place place;
    private double lon;
    private double lat;
    private String name;
}
