package com.litego.controller;

import com.litego.common.domain.R; // 根据你的项目调整 import
import com.litego.common.exception.CommonException;
import com.litego.common.utils.UserContext;
import com.litego.domain.dto.ItemDTO;
import com.litego.service.IItemService;
import com.litego.domain.vo.ItemLatestVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Tag(name = "健康数据接口")
@RestController
@RequestMapping("/qingtu")
@RequiredArgsConstructor
@Slf4j

//todo 目前已移除item业务，未来待实现
public class ItemController {

    private final IItemService itemService;

    @Operation(summary = "获取当前用户最近一条健康数据")
    @GetMapping
    public ItemLatestVO getLatestItemByUser() {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new CommonException("用户未登录", 401);
        }
        return itemService.getLatestByUserId(userId);
    }

    @Operation(summary = "添加当前用户的健康数据")
    @PostMapping
    public R<Void> addItem(@RequestBody ItemDTO dto) {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new CommonException("用户未登录", 401);
        }
        boolean success = itemService.addItem(userId, dto);
        if (success) {
            return R.ok();
        } else {
            return R.error("添加失败");
        }
    }
}
