package com.litego.controller;

import com.litego.domain.dto.MapQueryRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

@Tag(name = "地址相关接口")
@Slf4j
@RestController
@RequestMapping("/ai/map")
@RequiredArgsConstructor
public class MapController {

    private final ChatClient mapClient;

    @PostMapping
    @Operation(summary = "查询周边场所")
    public String search(@RequestBody MapQueryRequest request) {
        String prompt = request.getPrompt();
        log.info("收到用户查询：{}", prompt);
        String response = mapClient.prompt()
                .user(prompt)
                .call()
                .content();
        return response;
    }
}