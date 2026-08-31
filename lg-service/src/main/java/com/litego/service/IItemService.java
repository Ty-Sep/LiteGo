package com.litego.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.litego.domain.dto.ItemDTO;
import com.litego.domain.po.Item;
import com.litego.domain.vo.ItemLatestVO;

public interface IItemService extends IService<Item> {
    /**
     * 根据用户ID查询最近一条记录，并返回VO
     * @param userId 用户ID
     * @return 最近记录的VO（可能为null）
     */
    ItemLatestVO getLatestByUserId(Long userId);

    /**
     * 添加健康数据
     * @param userId 当前用户ID
     * @param dto 前端传入的数据
     * @return 是否成功
     */
    boolean addItem(Long userId, ItemDTO dto);
}