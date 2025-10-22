package com.example.mycoffeedemo.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mycoffeedemo.entity.OrderItem;
import com.example.mycoffeedemo.mapper.OrderItemMapper;
import com.example.mycoffeedemo.service.OrderItemService;
import org.springframework.stereotype.Service;

@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper,OrderItem> implements OrderItemService {

}
