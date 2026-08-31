package com.litego.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "健康数据DTO")
public class ItemDTO {
    @Schema(description = "日程安排")
    private String schedule;

    @Schema(description = "饮食建议")
    private String diet;

    @Schema(description = "注意事项")
    private String precautions;
}