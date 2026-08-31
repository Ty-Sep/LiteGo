package com.litego.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "weather")
public class WeatherProperties {
    private Amap amap;

    @Data
    public static class Amap {
        private String url;
        private String key;
    }
}