package com.example.mycoffeedemo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("reviews")
public class Review {
    @TableId
    private Long id;
    private Long userId;
    private Long productId;
    private Long orderId;
    private Integer rating;            // 评分1-5
    private String comment;
    private String images;             // JSON存储图片URL数组
    private LocalDateTime createdAt;
}

