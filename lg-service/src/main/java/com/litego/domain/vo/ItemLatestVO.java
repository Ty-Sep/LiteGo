package com.litego.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户最近一条健康数据")
public class ItemLatestVO {
    @Schema(description = "日程安排")
    private String schedule;

    @Schema(description = "饮食建议")
    private String diet;

    @Schema(description = "注意事项")
    private String precautions;
}