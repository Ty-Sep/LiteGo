package com.litego.controller;

import com.litego.domain.dto.ChatRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Tag(name = "菜谱相关接口")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class RecipeController {

    private final ChatClient recipeChatClient;

    @RequestMapping(value = "/recipe")
    @Operation(summary = "根据用户信息生成菜谱")
    public Flux<String> chat(@RequestBody ChatRequest chatRequest) {
        // 1. 获取当前时间并判断餐别
        LocalTime now = LocalTime.now();
        String currentTime = now.format(DateTimeFormatter.ofPattern("HH:mm"));
        String mealPeriod = getMealPeriod(now);

        // 2. 构建用户提示（包含时间、用户需求、健康约束）
        String userPrompt = String.format("""
                当前时间：%s（%s）
                用户需求：%s
                
                请严格按照系统指令输出 JSON 格式的菜谱推荐。
                """, currentTime, mealPeriod, chatRequest.getPrompt());

        // 3. 调用 AI 流式返回
        return recipeChatClient.prompt()
                .user(userPrompt)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, chatRequest.getChatId()))
                .stream()
                .content();
    }

    private String getMealPeriod(LocalTime time) {
        int hour = time.getHour();
        if (hour >= 5 && hour < 10) return "早餐";
        else if (hour >= 10 && hour < 14) return "午餐";
        else if (hour >= 14 && hour < 21) return "晚餐";
        else return "加餐/轻食";
    }
}