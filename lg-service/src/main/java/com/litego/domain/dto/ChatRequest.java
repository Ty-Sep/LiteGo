// ChatRequest.java
package com.litego.domain.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String prompt;
    private String chatId;
}