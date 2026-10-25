package com.weather.AirQuality.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WaqiStationSearch {

    public List<WaqiStation> all(String responseBody) throws JSONException {
        return matching(responseBody, "");
    }

    public List<WaqiStation> matching(String responseBody, String fragment) throws JSONException {
        JSONObject json = new JSONObject(responseBody);
        if (!"ok".equals(json.optString("status"))) {
            return List.of();
        }
        JSONArray data = json.optJSONArray("data");
        if (data == null) {
            return List.of();
        }

        String needle = fragment.toLowerCase(Locale.ROOT);
        List<WaqiStation> matches = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject item = data.optJSONObject(i);
            if (item == null || !item.has("uid")) {
                continue;
            }
            JSONObject station = item.optJSONObject("station");
            if (station == null) {
                continue;
            }
            String name = station.optString("name", "");
            if (name.toLowerCase(Locale.ROOT).contains(needle)) {
                matches.add(new WaqiStation(item.getInt("uid"), name));
            }
        }
        return matches;
    }

    public record WaqiStation(int uid, String name) {
    }
}
