package com.example.mycoffeedemo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mycoffeedemo.annotation.RequireOwner;
import com.example.mycoffeedemo.dto.OrderResponseDTO;
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
    public Result<IPage<UserResponseDTO>> getUser(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size){
        return userService.getAllUser(page,size);
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

    //更新用户基本信息
    @RequireOwner(resource = "user", idArg = "id", allowRoles = {"admin"})
    @PutMapping("/{id}")
    public Result<UserResponseDTO> updateUser(@PathVariable Long id,
                                              @RequestBody UserRequestDTO dto) {
        return userService.updateUser(id, dto);
    }

    /**
     * 管理员重置用户密码
     * 重置为：邮箱 + 123
     */
    @RequireOwner(resource = "user", idArg = "userId", allowRoles = {"admin"})
    @PutMapping("/{userId}/reset-password")
    public Result<String> resetPassword(@RequestParam Long userId){
        return userService.resetPassword(userId);
    }

    /**
     * 管理员新增用户
     */
    @RequireOwner( resource = "user",
            idArg = "id",
            allowRoles = {"admin"})
    @PostMapping
    public Result<UserResponseDTO> addUser(@RequestBody UserRequestDTO dto) {
        return userService.addUser(dto);
    }

    /**
     * 获取手机发送验证码
     */
    @GetMapping("/code")
    public Result<String> codePhone(String phone) {
        return userService.codePhone(phone);
    }

    /**
     * 获取手机发送验证码
     */
    @PostMapping("/phoneLogin")
    public Result<UserResponseDTO> phoneLogin(String phone,String code) {
        return userService.phoneLogin(phone,code);
    }

}
