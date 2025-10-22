package com.example.mycoffeedemo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String avatarUrl;
    private String role;
    private LocalDateTime createdAt;
    private String status;  // active/frozen/deleted
    private String token;   // 登录后返回的 JWT
}
