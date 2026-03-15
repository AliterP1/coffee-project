package com.example.mycoffeedemo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "products", autoResultMap = true)
public class Product {
    @TableId
    private Long id;
    private Long merchantId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private Boolean isActive;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images;             // JSON 存储图片URL数组
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

