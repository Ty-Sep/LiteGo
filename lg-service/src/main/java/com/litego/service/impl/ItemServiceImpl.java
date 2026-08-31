package com.litego.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.litego.domain.dto.ItemDTO;
import com.litego.domain.po.Item;
import com.litego.mapper.ItemMapper;
import com.litego.service.IItemService;
import com.litego.domain.vo.ItemLatestVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements IItemService {

    @Override
    public ItemLatestVO getLatestByUserId(Long userId) {
        // 查询最近一条记录
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<Item>()
                .eq(Item::getUserId, userId)
                .orderByDesc(Item::getUpdateTime)
                .last("LIMIT 1");
        Item item = this.getOne(wrapper);

        if (item == null) {
            return null;
        }

        // 封装VO
        ItemLatestVO vo = new ItemLatestVO();
        vo.setSchedule(item.getSchedule());
        vo.setDiet(item.getDiet());
        vo.setPrecautions(item.getPrecautions());
        return vo;
    }

    @Override
    @Transactional
    public boolean addItem(Long userId, ItemDTO dto) {
        Item item = new Item();
        item.setUserId(userId);
        item.setSchedule(dto.getSchedule());
        item.setDiet(dto.getDiet());
        item.setPrecautions(dto.getPrecautions());
        // 设置 status 默认值（假设 0 表示正常）
//        item.setStatus(0);
         item.setCreateTime(LocalDateTime.now());
         item.setUpdateTime(LocalDateTime.now());
        return save(item);
    }
}