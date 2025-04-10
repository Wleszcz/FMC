package com.FMC.FMC.heatMap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HeatPoint {
    private double lat;
    private double lon;
    private int value;
}
