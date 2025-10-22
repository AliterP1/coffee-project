package com.example.mycoffeedemo.entity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("coffee_contents")
public class CoffeeContent {
    @TableId
    private Long id;
    private String title;
    private String content;
    private String images;             // JSON存储图片URL数组
    private Long authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
