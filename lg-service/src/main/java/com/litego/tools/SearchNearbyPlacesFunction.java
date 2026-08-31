package com.litego.tools;

import com.litego.service.AmapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class SearchNearbyPlacesFunction {

    @Autowired
    private AmapService amapService;

    @Tool(name = "searchNearbyPlaces", description = "根据地址、关键词和距离范围，搜索附近符合条件的运动场所，如公园、篮球场、健身房等")
    public Map<String, Object> searchNearbyPlaces(
            @ToolParam(description = "详细的地址信息，如'广州市天河区天河城'") String address,
            @ToolParam(description = "搜索的关键词，如'公园'、'篮球场'、'健身房'") String keywords,
            @ToolParam(description = "搜索半径范围，单位为米，用户未指定时默认为1500") Integer radius) {
        log.info("调用 searchNearbyPlaces: address={}, keywords={}, radius={}", address, keywords, radius);
        var location = amapService.geocode(address);
        if (location == null) {
            return Map.of("error", "地址解析失败：" + address);
        }
        double lng = location.get("lng");
        double lat = location.get("lat");
        int actualRadius = (radius != null && radius > 0) ? radius : 1500;
        Map<String, Object> geojson = amapService.searchAround(lng, lat, actualRadius, keywords);
        return geojson;
    }
}