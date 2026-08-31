package com.litego.service;

import java.util.Map;

public interface AmapService {
    /**
     * 地理编码：地址 → GCJ-02 坐标
     */
    Map<String, Double> geocode(String address);

    /**
     * 周边搜索，返回 GeoJSON FeatureCollection
     */
    Map<String, Object> searchAround(double lng, double lat, int radius, String keywords);
}