package com.weather.AirQuality.service;

import org.json.JSONException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaqiStationSearchTest {

    private final WaqiStationSearch search = new WaqiStationSearch();

    @Test
    void matchesStationNameFragmentIgnoringCase() throws JSONException {
        List<WaqiStationSearch.WaqiStation> matches = search.matching("""
                {
                  "status": "ok",
                  "data": [
                    {
                      "uid": 5722,
                      "station": { "name": "Moscow, Spiridonovka, Russia" }
                    },
                    {
                      "uid": 6400,
                      "station": { "name": "Moscow, Shabolovka, Russia" }
                    }
                  ]
                }
                """, "spirid");

        assertEquals(1, matches.size());
        assertEquals(5722, matches.get(0).uid());
        assertEquals("Moscow, Spiridonovka, Russia", matches.get(0).name());
    }

    @Test
    void listsEveryStationWhenFragmentIsEmpty() throws JSONException {
        List<WaqiStationSearch.WaqiStation> stations = search.all("""
                {
                  "status": "ok",
                  "data": [
                    { "uid": 1, "station": { "name": "Moscow, Spiridonovka" } },
                    { "uid": 2, "station": { "name": "Moscow, Shabolovka" } }
                  ]
                }
                """);

        assertEquals(2, stations.size());
    }

    @Test
    void returnsEmptyWhenSearchFailed() throws JSONException {
        assertTrue(search.matching("""
                { "status": "error", "data": "Unknown station" }
                """, "spirid").isEmpty());
    }
}
