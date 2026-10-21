package com.weather.AirQuality.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class Pm25AdvisorTest {

    private final Pm25Advisor advisor = new Pm25Advisor();

    @Test
    void recommendsByPm25Thresholds() {
        assertTrue(advisor.recommend(15.0).contains("отличное"));
        assertTrue(advisor.recommend(15.1).contains("умеренное"));
        assertTrue(advisor.recommend(40.0).contains("умеренное"));
        assertTrue(advisor.recommend(40.1).contains("чувствительных"));
        assertTrue(advisor.recommend(65.0).contains("чувствительных"));
        assertTrue(advisor.recommend(65.1).contains("ограничить"));
        assertTrue(advisor.recommend(150.0).contains("ограничить"));
        assertTrue(advisor.recommend(150.1).contains("Очень плохое"));
        assertTrue(advisor.recommend(250.0).contains("Очень плохое"));
        assertTrue(advisor.recommend(250.1).contains("Опасное"));
        assertTrue(advisor.recommend(350.0).contains("Опасное"));
        assertTrue(advisor.recommend(350.1).contains("Чрезвычайно опасное"));
    }
}
