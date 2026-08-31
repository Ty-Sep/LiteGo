package com.litego.controller;

import com.litego.domain.dto.AddressItem;
import com.litego.domain.dto.ChatRequest;
import com.litego.domain.dto.ScheduleRequest;
import com.litego.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@Tag(name = "AI聊天和生成日程表相关接口")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class ChatAndScheduleController {

    private final ChatClient chatClient;
    private final ChatClient dateChatClient;
    private final WeatherService weatherService;

    @RequestMapping(value = "/chat")
    @Operation(summary = "AI对话")
    public Flux<String> chat(@RequestBody ChatRequest request) {
        return chatClient.prompt()
                .user(request.getPrompt())
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, request.getChatId()))
                .stream()
                .content();
    }

    @PostMapping("/date")
    @Operation(summary = "AI根据用户信息生成日程表")
    public String schedule(@RequestBody ScheduleRequest request) {
        // 无地址直接生成
        if (request.getAddress() == null || request.getAddress().isEmpty()) {
            log.info("无地址信息，直接生成一周日程表");
            return generateWeekSchedule(request, "未获取到天气信息");
        }

        // 使用省份+城市进行查询
        AddressItem addr = request.getAddress().get(0);
        String city = addr.getCity();
        String province = addr.getProvince();
        String queryCity = (city != null && !city.isEmpty()) ? city : province;

        // 无有效地址
        if (queryCity == null || queryCity.isEmpty()) {
            return generateWeekSchedule(request, "未获取到天气信息");
        }

        // 同步查询天气
        Map<String, Object> weatherData = weatherService.get7DayWeather(queryCity);
        String weatherText = weatherService.format7DayWeather(weatherData);
        return generateWeekSchedule(request, weatherText);
    }

    /**
     * 统一生成7天日程表
     */
    private String generateWeekSchedule(ScheduleRequest request, String weatherInfo) {
        String prompt = String.format("""
                用户需求：%s
                未来7天天气：%s
                """, request.getPrompt(), weatherInfo);

        log.info("传递给AI的天气信息：\n{}", weatherInfo);
        return dateChatClient.prompt()
                .user(prompt)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, request.getChatId()))
                .call()
                .content();
    }
}