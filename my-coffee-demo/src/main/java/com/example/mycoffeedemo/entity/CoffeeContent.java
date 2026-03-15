package com.example.mycoffeedemo.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "coffee_contents",autoResultMap = true)
public class CoffeeContent {
    @TableId
    private Long id;
    private String title;
    private String content;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images;           // JSON存储图片URL数组
    private Long authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
