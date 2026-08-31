package com.litego.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class ScheduleRequest {
    private List<AddressItem> address;
    private String prompt;
    private String chatId;

}