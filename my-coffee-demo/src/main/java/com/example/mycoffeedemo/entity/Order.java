package com.example.mycoffeedemo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    @TableId
    private Long id;
    private Long userId;
    private Long addressId;
    private BigDecimal totalPrice;
    private String status;             // pending/paid/shipped/completed/cancelled
    private String TradeNo;
    private String recipientName;
    private String phone;
    private String fullAddress;

    private LocalDateTime expireTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

