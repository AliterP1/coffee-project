package com.example.mycoffeedemo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId
    private Long id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String avatarUrl;          // 头像URL
    private String role;               // user/merchant/admin
    private String status;             // active/frozen/deleted
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

