package com.example.mycoffeedemo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.mycoffeedemo.dto.CreateOrderRequestDTO;
import com.example.mycoffeedemo.dto.OrderResponseDTO;
import com.example.mycoffeedemo.entity.Order;
import com.example.mycoffeedemo.service.OrderService;
import com.example.mycoffeedemo.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public Result<Order> createOrder(@RequestBody CreateOrderRequestDTO dto){
        return orderService.createOrder(dto);
    }

    @PostMapping("/{orderId}/cancel")
    public Result<Order> cancelOrder(@PathVariable Long orderId){
        return orderService.cancelOrder(orderId);
    }

    @PostMapping("orders")
    public Result<IPage<OrderResponseDTO>> getUserOrders(@RequestParam(defaultValue = "1") Long page,
                                                         @RequestParam(defaultValue = "10") Long size,
                                                         @RequestParam Long userId){
        return orderService.getUserOrders(page,size,userId);
    }

    @PostMapping("details")
    public Result<OrderResponseDTO> getOrderDetails(@RequestParam Long orderId){
        return orderService.getOrderDetails(orderId);
    }
}

