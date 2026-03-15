package com.example.mycoffeedemo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mycoffeedemo.dto.OrderResponseDTO;
import com.example.mycoffeedemo.dto.UserRequestDTO;
import com.example.mycoffeedemo.dto.UserResponseDTO;
import com.example.mycoffeedemo.entity.User;
import com.example.mycoffeedemo.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

public interface UserService extends IService<User> {
    Result<UserResponseDTO> login(UserRequestDTO dto);

    Result<UserResponseDTO> register(UserRequestDTO dto);

    Result<Void> changePassword(Long userId, String oldPassword, String newPassword);

    Result<String> updateStatus(Long userId, String status);

    Result<String> updateAvatar(Long id, MultipartFile file);

    Result<UserResponseDTO> getUserId(String id, HttpServletRequest request);

    Result<IPage<UserResponseDTO>> getAllUser(int page, int size);

    Result<UserResponseDTO> updateUser(Long id, UserRequestDTO dto);

    Result<String> resetPassword(Long userId);

    Result<UserResponseDTO> addUser(UserRequestDTO dto);

    Result<String> codePhone(String phone);

    Result<UserResponseDTO> phoneLogin(String phone,String code);
}
