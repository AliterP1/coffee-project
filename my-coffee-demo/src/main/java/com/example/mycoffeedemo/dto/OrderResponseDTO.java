package com.example.mycoffeedemo.dto;

import com.example.mycoffeedemo.entity.Address;
import com.example.mycoffeedemo.entity.Order;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDTO {
    private Long id;
    private Long userId;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime expireTime;
    private List<OrderItemResponseDTO> items;
    private List<AddressResponseDTO> address;

    @Data
    public static class OrderItemResponseDTO {
        private Long productId;
        private Integer quantity;
        private BigDecimal price;
        private String productName;
        private List<String> productImages;
    }

    @Data
    public static class AddressResponseDTO {
        private String recipientName;
        private String phone;
        private String fullAddress;
    }
}
