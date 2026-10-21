package com.weather.AirQuality.service;

import com.weather.AirQuality.http.AirQualityInterface;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AirQualityService {

    private final AirQualityInterface airQualityInterface;
    private final BotMessenger botMessenger;
    private final WaqiResponseParser parser;
    private final Pm25Advisor advisor;

    @Autowired
    public AirQualityService(AirQualityInterface airQualityInterface, BotMessenger botMessenger) {
        this(airQualityInterface, botMessenger, new WaqiResponseParser(), new Pm25Advisor());
    }

    AirQualityService(AirQualityInterface airQualityInterface,
                      BotMessenger botMessenger,
                      WaqiResponseParser parser,
                      Pm25Advisor advisor) {
        this.airQualityInterface = airQualityInterface;
        this.botMessenger = botMessenger;
        this.parser = parser;
        this.advisor = advisor;
    }

    public void sendAirQualityNow(long chatId, String city) {
        try {
            String normalizedCity = city.toLowerCase();
            ResponseEntity<String> response = airQualityInterface.getAirQualityData(normalizedCity);
            String responseBody = response.getBody();

            if (responseBody == null) {
                botMessenger.send(chatId, "Город не найден. Пожалуйста, проверьте написание и попробуйте снова.");
                return;
            }

            JSONObject jsonResponse = new JSONObject(responseBody);
            String status = jsonResponse.optString("status", "error");
            log.info("API response status {} for city {}", status, normalizedCity);
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
