package com.weather.AirQuality.service;

import org.json.JSONException;
import org.json.JSONObject;

public class WaqiResponseParser {

    public AirQualitySnapshot parse(JSONObject jsonResponse) throws JSONException {
        JSONObject data = jsonResponse.getJSONObject("data");
        return new AirQualitySnapshot(
                extractCityName(data),
                extractPm25Value(data),
                extractMeasurement(data, "t", "температуры"),
                extractMeasurement(data, "h", "влажности")
        );
    }

    private double extractPm25Value(JSONObject data) throws JSONException {
        JSONObject iaqi = data.optJSONObject("iaqi");
        if (iaqi == null || !iaqi.has("pm25")) {
            throw new JSONException("Данные о PM2.5 не найдены");
        }

        Object value = iaqi.getJSONObject("pm25").opt("v");
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String text) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException e) {
                throw new JSONException("Неверный формат значения PM2.5: " + value);
            }
        }
        if (value == null) {
            throw new JSONException("Данные о PM2.5 не найдены");
        }
        throw new JSONException("Неожиданный тип значения для PM2.5: " + value.getClass().getName());
    }

    private String extractCityName(JSONObject data) throws JSONException {
        return data.getJSONObject("city").getString("name");
    }

    private double extractMeasurement(JSONObject data, String key, String label) throws JSONException {
        JSONObject iaqi = data.getJSONObject("iaqi");
        if (!iaqi.has(key)) {
            throw new JSONException("Данные о " + label + " не найдены");
        }
        return iaqi.getJSONObject(key).getDouble("v");
    }
}
