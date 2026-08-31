package com.litego.service;

import java.util.Map;

public interface WeatherService {
    Map<String, Object> get7DayWeather(String city);
    String format7DayWeather(Map<String, Object> weatherData);
}