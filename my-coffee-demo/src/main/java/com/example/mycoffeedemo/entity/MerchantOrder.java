package com.example.mycoffeedemo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("merchant_orders")
public class MerchantOrder {
    private Long id;
    private Long merchantId;
    private Long orderId;
    private String status;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
}
