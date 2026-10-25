package com.weather.AirQuality.service;

public record CityQuery(String city, String stationFragment) {

    public static CityQuery parse(String text) {
        String trimmed = text == null ? "" : text.trim();
        int comma = trimmed.indexOf(',');
        if (comma < 0) {
            return new CityQuery(trimmed, "");
        }
        return new CityQuery(
                trimmed.substring(0, comma).trim(),
                trimmed.substring(comma + 1).trim()
        );
    }

    public boolean hasStation() {
        return !stationFragment.isBlank();
    }
}
