package com.weather.AirQuality.service;

import com.weather.AirQuality.http.AirQualityInterface;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class AirQualityService {

    private final AirQualityInterface airQualityInterface;
    private final BotMessenger botMessenger;
    private final WaqiResponseParser parser;
    private final Pm25Advisor advisor;
    private final WaqiStationSearch stationSearch;

    @Autowired
    public AirQualityService(AirQualityInterface airQualityInterface, BotMessenger botMessenger) {
        this(airQualityInterface, botMessenger, new WaqiResponseParser(), new Pm25Advisor(), new WaqiStationSearch());
    }

    AirQualityService(AirQualityInterface airQualityInterface,
                      BotMessenger botMessenger,
                      WaqiResponseParser parser,
                      Pm25Advisor advisor,
                      WaqiStationSearch stationSearch) {
        this.airQualityInterface = airQualityInterface;
        this.botMessenger = botMessenger;
        this.parser = parser;
        this.advisor = advisor;
        this.stationSearch = stationSearch;
    }

    public void sendAirQualityNow(long chatId, String text) {
        CityQuery query = CityQuery.parse(text);
        if (query.city().isBlank()) {
            botMessenger.send(chatId, "Город не найден. Пожалуйста, проверьте написание и попробуйте снова.");
            return;
        }
        if (query.hasStation()) {
            sendStation(chatId, query);
            return;
        }
        sendFeed(chatId, query.city().toLowerCase(Locale.ROOT));
    }

    private void sendStation(long chatId, CityQuery query) {
        try {
            String keyword = query.city().toLowerCase(Locale.ROOT);
            ResponseEntity<String> response = airQualityInterface.searchStations(keyword);
            String responseBody = response.getBody();
            if (responseBody == null) {
                botMessenger.send(chatId, "Станция не найдена. Уточните название и попробуйте снова.");
                return;
            }

            List<WaqiStationSearch.WaqiStation> matches = stationSearch.matching(responseBody, query.stationFragment());
            log.info("Station search for {} / {} returned {} matches", keyword, query.stationFragment(), matches.size());
            if (matches.isEmpty()) {
                botMessenger.send(chatId, "Станция не найдена. Уточните название и попробуйте снова.");
                return;
            }
            if (matches.size() > 1) {
                botMessenger.send(chatId, severalStationsMessage(matches));
                return;
            }
            sendFeed(chatId, "@" + matches.get(0).uid());
        } catch (Exception e) {
            log.error("Station search failed: {}", e.getMessage());
            botMessenger.send(chatId, "Станция не найдена. Уточните название и попробуйте снова.");
        }
    }

    private String severalStationsMessage(List<WaqiStationSearch.WaqiStation> matches) {
        int shown = Math.min(matches.size(), 8);
        StringBuilder message = new StringBuilder("Найдено несколько станций. Уточните название:");
        for (int i = 0; i < shown; i++) {
            message.append('\n').append(matches.get(i).name());
        }
        if (matches.size() > shown) {
            message.append("\nи ещё ").append(matches.size() - shown);
        }
        return message.toString();
    }

    private void sendFeed(long chatId, String feed) {
        try {
            ResponseEntity<String> response = airQualityInterface.getAirQualityData(feed);
            String responseBody = response.getBody();

            if (responseBody == null) {
                botMessenger.send(chatId, "Город не найден. Пожалуйста, проверьте написание и попробуйте снова.");
                return;
            }

            JSONObject jsonResponse = new JSONObject(responseBody);
            String status = jsonResponse.optString("status", "error");
            log.info("API response status {} for feed {}", status, feed);
            if (!"ok".equals(status)) {
                String errorMessage = jsonResponse.optString("data", "неизвестная ошибка");
                botMessenger.send(chatId, "Ошибка при получении данных: " + errorMessage);
                return;
            }

            sendAirQualityUpdate(jsonResponse, chatId);
        } catch (JSONException e) {
            log.error("Failed to parse air quality response: {}", e.getMessage());
            botMessenger.send(chatId, "Ошибка при обработке данных. Пожалуйста, проверьте написание города и попробуйте снова.");
        } catch (Exception e) {
            log.error("Air quality request failed: {}", e.getMessage());
            botMessenger.send(chatId, "Город не найден. Пожалуйста, проверьте написание и попробуйте снова.");
        }
    }

    private void sendAirQualityUpdate(JSONObject jsonResponse, long chatId) {
        try {
            AirQualitySnapshot snapshot = parser.parse(jsonResponse);
            String recommendation = advisor.recommend(snapshot.pm25());
            String message = String.format(
                    """
                            🌍 *Качество воздуха*
                            Город: *%s*
                            PM2.5: *%.1f µg/m³*
                            Температура: *%.1f°C*
                            Влажность: *%.1f%%*
                            Рекомендации: *%s*
                            """,
                    snapshot.cityName(), snapshot.pm25(), snapshot.temperature(), snapshot.humidity(), recommendation
            );
            log.info("Sending air quality update to chat {}", chatId);
            botMessenger.sendMarkdown(chatId, message);
        } catch (Exception e) {
            log.error("Failed to build air quality message: {}", e.getMessage());
            botMessenger.send(chatId, "Ошибка при обработке данных. Пожалуйста, попробуйте снова.");
        }
    }
}
