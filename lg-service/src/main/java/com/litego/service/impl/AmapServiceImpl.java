package com.litego.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.litego.service.AmapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 高德地图服务实现类
 * 功能：地理编码（地址→坐标）、周边场所搜索
 */
@Slf4j
@Service
public class AmapServiceImpl implements AmapService {

    @Value("${amap.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();


    /**
     * 地理编码：将地址转换为经纬度坐标
     * @param address 详细地址
     * @return 包含 lng（经度）和 lat（纬度）的 Map，失败返回 null
     */
    @Override
    public Map<String, Double> geocode(String address) {
        log.info("地理编码：{}", address);
        // 1. 构建请求 URL（高德地理编码 API）
        String url = String.format("https://restapi.amap.com/v3/geocode/geo?key=%s&address=%s", apiKey, address);
        try {
            // 2. 调用 API 并解析 JSON 响应
            String resp = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(resp);
            // 3.1 如果成功，返回状态 1
            if ("1".equals(root.path("status").asText())) {
                JsonNode geo = root.path("geocodes").get(0);
                String location = geo.path("location").asText();
                String[] lngLat = location.split(",");
                Map<String, Double> result = new HashMap<>();
                result.put("lng", Double.parseDouble(lngLat[0]));
                result.put("lat", Double.parseDouble(lngLat[1]));
                return result;
            }
        } catch (Exception e) {
            // 3.2 如果失败，报错
            log.error("地理编码失败", e);
        }
        return null;
    }

    /**
     * 周边搜索：根据经纬度、关键词和半径搜索附近场所
     * @param lng 经度
     * @param lat 纬度
     * @param radius 搜索半径（米）
     * @param keywords 搜索关键词（如"公园"、"篮球场"）
     * @return GeoJSON 格式的 FeatureCollection
     */
    @Override
    public Map<String, Object> searchAround(double lng, double lat, int radius, String keywords) {
        // 1. 构建请求 URL
        String url = String.format("https://restapi.amap.com/v5/place/around?key=%s&location=%f,%f&radius=%d&keywords=%s&output=JSON",
                apiKey, lng, lat, radius, keywords);
        // 2. 准备 GeoJSON 容器
        Map<String, Object> featureCollection = new HashMap<>();
        featureCollection.put("type", "FeatureCollection");
        List<Map<String, Object>> features = new ArrayList<>();

        try {
            // 3. 调用 API 并解析响应
            String resp = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(resp);
            if ("1".equals(root.path("status").asText())) {
                for (JsonNode poi : root.path("pois")) {
                    Map<String, Object> feature = new HashMap<>();
                    feature.put("type", "Feature");

                    // geometry
                    Map<String, Object> geometry = new HashMap<>();
                    geometry.put("type", "Point");
                    String[] loc = poi.path("location").asText().split(",");
                    double gcjLng = Double.parseDouble(loc[0]);
                    double gcjLat = Double.parseDouble(loc[1]);

                    // double[] wgs84 = coordTransform.gcj02ToWgs84(gcjLng, gcjLat);
                    // geometry.put("coordinates", new double[]{wgs84[0], wgs84[1]});

                    geometry.put("coordinates", new double[]{gcjLng, gcjLat});
                    feature.put("geometry", geometry);

                    // properties
                    Map<String, Object> properties = new HashMap<>();
                    properties.put("name", poi.path("name").asText());
                    properties.put("address", poi.path("address").asText());
                    properties.put("type", poi.path("type").asText());
                    feature.put("properties", properties);

                    features.add(feature);
                }
            }
        } catch (Exception e) {
            log.error("周边搜索失败", e);
        }
        // 组装最后结果并返回
        featureCollection.put("features", features);
        return featureCollection;
    }
}