package com.example.mycoffeedemo.dto;

import lombok.Data;

@Data
public class UserRequestDTO {
    private String username;
    private String password;
    private String email;
    private String phone;
    private String avatarUrl;
}

