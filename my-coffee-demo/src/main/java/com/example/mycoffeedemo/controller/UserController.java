package com.example.mycoffeedemo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mycoffeedemo.annotation.RequireOwner;
import com.example.mycoffeedemo.dto.UserRequestDTO;
import com.example.mycoffeedemo.dto.UserResponseDTO;
import com.example.mycoffeedemo.entity.User;
import com.example.mycoffeedemo.service.UserService;
import com.example.mycoffeedemo.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public IPage<User> getUser(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int size){
        return userService.page(new Page<>(page,size));
    }

    @GetMapping("/{id}")
    public Result<UserResponseDTO> getUser(@PathVariable String id, HttpServletRequest request) {
        return userService.getUserId(id,request);
    }

    @PostMapping("/login")
    public Result<UserResponseDTO> login(@RequestBody UserRequestDTO dto){
        return userService.login(dto);
    }

    @PostMapping("/register")
    public Result<UserResponseDTO> register(@RequestBody UserRequestDTO dto){
        return userService.register(dto);
    }


    @PostMapping("change-password")
    public Result<Void> changePassword(@RequestParam Long userId,
                                       @RequestParam String oldPassword,
                                       @RequestParam String newPassword){
        return userService.changePassword(userId,oldPassword,newPassword);
    }

    /**
     * 修改用户状态
     * @param userId 用户ID
     * @param status 新状态：active / frozen / deleted
     */
    @RequireOwner(resource = "user",idArg = "userId",allowRoles = {"admin"})
    @PutMapping("/status")
    public Result<String> updateStatus(@RequestParam Long userId,@RequestParam String status){
        return userService.updateStatus(userId,status);
    }

    //用户头像上传
    @RequireOwner(resource = "user",idArg = "id",allowRoles = {"admin","user","merchant"})
    @PostMapping("/{id}/avatar")
    public Result<String> updateAvatar(@PathVariable Long id, @RequestParam("file") MultipartFile file){
        return userService.updateAvatar(id,file);
    }
}
