package com.example.mycoffeedemo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("order_items")
public class OrderItem {
    @TableId
    private Long id;
    private Long orderId;
    private Long productId;
    private Long merchantOrderId;
    private Integer quantity;
    private BigDecimal price;
}

