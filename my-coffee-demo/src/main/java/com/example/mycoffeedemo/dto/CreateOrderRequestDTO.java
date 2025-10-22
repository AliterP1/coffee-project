package com.example.mycoffeedemo.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequestDTO {
    private Long userId;
    private Long addressId;
    private List<OrderItemDTO> items;

    @Data
    public static class OrderItemDTO {
        private Long productId;
        private Integer quantity;
    }
}

