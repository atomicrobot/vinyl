package com.madebyatomicrobot.vinyl.samples;

import com.madebyatomicrobot.vinyl.annotations.Record;
import com.madebyatomicrobot.vinyl.annotations.Projection;

@Record
public interface Planet {
    @Projection(PlanetContract.Planet._ID)
    long id();

    String name();

    @Projection(PlanetContract.Planet.COLUMN_NAME_RADIUS_IN_METERS)
    double radiusInMeters();

    @Projection(PlanetContract.Planet.COLUMN_NAME_INHABITED_BY_HUMANS)
    boolean inhabitedByHumans();
}
