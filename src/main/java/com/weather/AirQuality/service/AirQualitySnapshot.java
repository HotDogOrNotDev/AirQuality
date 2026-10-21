package com.weather.AirQuality.service;

public record AirQualitySnapshot(String cityName, double pm25, double temperature, double humidity) {
}
