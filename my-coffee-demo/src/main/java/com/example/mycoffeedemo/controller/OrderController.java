package com.example.mycoffeedemo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.mycoffeedemo.dto.CreateOrderRequestDTO;
import com.example.mycoffeedemo.dto.OrderResponseDTO;
import com.example.mycoffeedemo.entity.Order;
import com.example.mycoffeedemo.service.OrderService;
import com.example.mycoffeedemo.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("merchantOrder")
    public Result<IPage<OrderResponseDTO>> getMerchantOrder(@RequestParam(defaultValue = "1") Long page,
                                                            @RequestParam(defaultValue = "10") Long size,
                                                            @RequestParam Long userId){
        return  orderService.getMerchantOrder(page,size,userId);
    }

    @PostMapping("allOrder")
    public Result<IPage<OrderResponseDTO>> getAllOrder(@RequestParam(defaultValue = "1") Long page,
                                                       @RequestParam(defaultValue = "10") Long size){
        return  orderService.getAllOrder(page,size);
    }

    /**
     * 商家发货接口
     * @param merchantOrderId 子订单ID
     * @return Result
     */
    @PostMapping("/{merchantOrderId}/ship")
    public Result<String> shipOrder(@PathVariable Long merchantOrderId) {
        return orderService.ship(merchantOrderId);
    }
}

