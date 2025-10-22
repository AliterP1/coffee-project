package com.example.mycoffeedemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mycoffeedemo.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
