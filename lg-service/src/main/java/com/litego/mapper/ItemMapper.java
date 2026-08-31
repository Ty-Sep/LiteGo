package com.litego.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.litego.domain.po.Item;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ItemMapper extends BaseMapper<Item> {
}