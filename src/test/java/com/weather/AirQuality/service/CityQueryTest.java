package com.weather.AirQuality.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CityQueryTest {

    @Test
    void keepsCityWithoutComma() {
        CityQuery query = CityQuery.parse("  Moscow ");

        assertEquals("Moscow", query.city());
        assertFalse(query.hasStation());
    }

    @Test
    void splitsStationWithOrWithoutSpace() {
        CityQuery spaced = CityQuery.parse("Moscow, Spirid");
        CityQuery tight = CityQuery.parse("Moscow,Spirid");

        assertEquals("Moscow", spaced.city());
        assertEquals("Spirid", spaced.stationFragment());
        assertTrue(spaced.hasStation());
        assertEquals("Spirid", tight.stationFragment());
    }

    @Test
    void ignoresEmptyStation() {
        CityQuery query = CityQuery.parse("Moscow,");

        assertEquals("Moscow", query.city());
        assertFalse(query.hasStation());
    }
}
