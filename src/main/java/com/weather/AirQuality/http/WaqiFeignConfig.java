package com.weather.AirQuality.http;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class WaqiFeignConfig {

    @Bean
    public RequestInterceptor waqiTokenInterceptor(@Value("${waqi.token}") String token) {
        return template -> template.query("token", token);
    }
}
