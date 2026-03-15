package com.example.mycoffeedemo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mycoffeedemo.dto.CreateOrderRequestDTO;
import com.example.mycoffeedemo.dto.OrderResponseDTO;
import com.example.mycoffeedemo.entity.Order;
import com.example.mycoffeedemo.utils.Result;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;

public interface OrderService extends IService<Order> {
    Result<Order> createOrder(CreateOrderRequestDTO dto);

    Result<String> getNotify(HttpServletRequest request) throws Exception;

    Result<Order> cancelOrder(Long orderId);

    Result<String> aiPay(String orderId) throws Exception;

    Result<OrderResponseDTO> getUserCart(Long userId);

    Result<String> addToCart(Long userId,Long productId, Integer quantity);

    Result<String> updateCartItem(Long userId,Long productId, Integer quantity);

    Result<String> removeFromCart(Long userId,Long productId);

    Result<String> clearCart(Long userId);

    Result<IPage<OrderResponseDTO>> getUserOrders(Long page,Long size,Long userId);

    Result<OrderResponseDTO> getOrderDetails(Long orderId);

    Result<IPage<OrderResponseDTO>> getAllOrder(Long page,Long size);

    Result<String> getPaid(String orderId);

    Result<IPage<OrderResponseDTO>> getMerchantOrder(Long page, Long size, Long userId);

    Result<String> ship(Long merchantOrderId);
}
