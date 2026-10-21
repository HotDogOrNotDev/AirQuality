package com.weather.AirQuality.http;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "waqi", url = "${waqi.url}", configuration = WaqiFeignConfig.class)
public interface AirQualityInterface {

    @GetMapping("/{city}/")
    ResponseEntity<String> getAirQualityData(@PathVariable("city") String city);
}
