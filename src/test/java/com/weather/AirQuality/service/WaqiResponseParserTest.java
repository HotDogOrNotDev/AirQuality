package com.weather.AirQuality.service;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WaqiResponseParserTest {

    private final WaqiResponseParser parser = new WaqiResponseParser();

    @Test
    void parsesNumericMeasurements() throws JSONException {
        AirQualitySnapshot snapshot = parser.parse(new JSONObject("""
                {
                  "status": "ok",
                  "data": {
                    "city": { "name": "Moscow, Russia" },
                    "iaqi": {
                      "pm25": { "v": 12.5 },
                      "t": { "v": 3 },
                      "h": { "v": 80 }
                    }
                  }
                }
                """));

        assertEquals("Moscow, Russia", snapshot.cityName());
        assertEquals(12.5, snapshot.pm25());
        assertEquals(3.0, snapshot.temperature());
        assertEquals(80.0, snapshot.humidity());
    }

    @Test
    void parsesPm25ProvidedAsString() throws JSONException {
        AirQualitySnapshot snapshot = parser.parse(new JSONObject("""
                {
                  "data": {
                    "city": { "name": "London" },
                    "iaqi": {
                      "pm25": { "v": "18.5" },
                      "t": { "v": 11.0 },
                      "h": { "v": 70.0 }
                    }
                  }
                }
                """));

        assertEquals(18.5, snapshot.pm25());
    }

    @Test
    void rejectsResponseWithoutPm25() throws JSONException {
        JSONObject response = new JSONObject("""
                {
                  "data": {
                    "city": { "name": "Paris" },
                    "iaqi": { "t": { "v": 15 }, "h": { "v": 60 } }
                  }
                }
                """);

        assertThrows(JSONException.class, () -> parser.parse(response));
    }
}
