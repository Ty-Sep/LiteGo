package com.litego.service.impl;

import com.litego.config.WeatherProperties;
import com.litego.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final WeatherProperties weatherProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    // 国内热门城市 城市名→adcode 映射
    private static final Map<String, String> CITY_ADCODE_MAP = new HashMap<>();
    static {
        CITY_ADCODE_MAP.put("广州", "440100");
        CITY_ADCODE_MAP.put("北京", "110000");
        CITY_ADCODE_MAP.put("上海", "310000");
        CITY_ADCODE_MAP.put("深圳", "440300");
        CITY_ADCODE_MAP.put("杭州", "330100");
        CITY_ADCODE_MAP.put("成都", "510100");
        CITY_ADCODE_MAP.put("重庆", "500000");
        CITY_ADCODE_MAP.put("武汉", "420100");
        CITY_ADCODE_MAP.put("南京", "320100");
        CITY_ADCODE_MAP.put("西安", "610100");
    }

    @Override
    public Map<String, Object> get7DayWeather(String city) {
        log.info("【高德天气】查询天气预报，城市：{}", city);
        String url = weatherProperties.getAmap().getUrl();
        String key = weatherProperties.getAmap().getKey();

        // 城市名转adcode，默认广州
        String adcode = CITY_ADCODE_MAP.getOrDefault(city, "440100");
        // extensions=all 表示获取预报，base为实况
        String uri = String.format("%s?key=%s&city=%s&extensions=all",
                url, key, adcode);
        log.info("【高德天气】请求URL：{}", uri);

        try {
            Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
            log.info("【高德天气】查询成功");
            return response;
        } catch (Exception e) {
            log.error("【高德天气】查询失败", e);
            return new HashMap<>();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public String format7DayWeather(Map<String, Object> weatherData) {
        if (weatherData == null || weatherData.isEmpty()) {
            return "无法获取天气预报信息";
        }

        // 校验高德接口返回状态
        String status = (String) weatherData.get("status");
        if (!"1".equals(status)) {
            String info = (String) weatherData.get("info");
            log.error("【高德天气】接口错误：{}", info);
            return "天气服务异常：" + info;
        }

        try {
            // 解析预报数据
            List<Map<String, Object>> forecasts = (List<Map<String, Object>>) weatherData.get("forecasts");
            if (forecasts == null || forecasts.isEmpty()) {
                return "未查询到天气预报数据";
            }

            Map<String, Object> forecast = forecasts.get(0);
            String cityName = (String) forecast.get("city");
            List<Map<String, Object>> casts = (List<Map<String, Object>>) forecast.get("casts");

            StringBuilder sb = new StringBuilder();
            sb.append(cityName).append(" 未来天气预报\n");

            // 遍历预报天数
            for (int i = 0; i < casts.size(); i++) {
                Map<String, Object> day = casts.get(i);
                String date = day.get("date").toString();
                String dayWeather = day.get("dayweather").toString();
                String nightWeather = day.get("nightweather").toString();
                String tempMax = day.get("daytemp").toString();
                String tempMin = day.get("nighttemp").toString();
                String windDir = day.get("daywind").toString();

                sb.append(String.format("第%d天(%s)：白天%s，夜间%s，气温%s℃~%s℃，风向：%s\n",
                        i + 1, date, dayWeather, nightWeather, tempMin, tempMax, windDir));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("【高德天气】数据解析失败", e);
            return "天气数据解析失败";
        }
    }
}