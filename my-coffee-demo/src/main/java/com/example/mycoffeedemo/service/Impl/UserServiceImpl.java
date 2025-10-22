package com.example.mycoffeedemo.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mycoffeedemo.dto.UserRequestDTO;
import com.example.mycoffeedemo.dto.UserResponseDTO;
import com.example.mycoffeedemo.entity.User;
import com.example.mycoffeedemo.mapper.UserMapper;
import com.example.mycoffeedemo.service.FileService;
import com.example.mycoffeedemo.service.UserService;
import com.example.mycoffeedemo.utils.CurrentUserUtil;
import com.example.mycoffeedemo.utils.JwtUtils;
import com.example.mycoffeedemo.utils.PhoneUtil;
import com.example.mycoffeedemo.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils  jwtUtils;

    @Autowired
    private FileService fileService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Override
    public Result<UserResponseDTO> login(UserRequestDTO dto) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("email", dto.getEmail()));
        if(user == null){
            return Result.fail(404, "用户不存在");
        }
        if ("frozen".equals(user.getStatus())) {
            return Result.fail(403, "用户已被冻结");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return Result.fail(400, "用户名或密码错误");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());
        claims.put("email", user.getEmail());
        String token = jwtUtils.generateToken(dto.getUsername(),claims);

        UserResponseDTO responseDTO = new UserResponseDTO();
        BeanUtils.copyProperties(user, responseDTO);
        responseDTO.setToken(token);

        return Result.success("登录成功！",responseDTO);
    }

    @Override
    public Result<UserResponseDTO> register(UserRequestDTO dto) {
        //1、判断用户是否存在
        User exits = userMapper.selectOne(new QueryWrapper<User>().eq("email", dto.getEmail()));
        if(exits!=null){
            return Result.fail(404,"用户名已存在");
        }
        if (dto.getPhone() != null && !PhoneUtil.isValidPhone(dto.getPhone())) {
            return Result.fail(403,"手机格式错误");
        }
        //2、密码加密
        String password = passwordEncoder.encode(dto.getPassword());

        //3、保存用户
        User user = new User();
        BeanUtils.copyProperties(dto,user);
        user.setPassword(password);
        userMapper.insert(user);

        UserResponseDTO responseDTO = new UserResponseDTO();
        BeanUtils.copyProperties(user, responseDTO);
        return Result.success("注册成功",responseDTO);
    }

    @Override
    public Result<Void> changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);

        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        if("frozen".equals(user.getStatus())){
            return Result.fail(403,"用户正在冻结，静止修改密码");
        }
        // 1. 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.fail(400, "旧密码错误");
        }

        // 2. 加密新密码
        String newHash = passwordEncoder.encode(newPassword);
        user.setPassword(newHash);

        // 3. 更新数据库
        int update = userMapper.updateById(user);
        if (update > 0) {
            return Result.success( "密码修改成功",null);
        } else {
            return Result.fail(500, "密码修改失败");
        }
    }

    @Override
    public Result<String> updateStatus(Long userId, String status) {
        //检查status状态合法性
        if (!"active".equals(status)&&!"frozen".equals(status) && !"deleted".equals(status)) {
            return Result.fail(400, "非法状态");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
        return Result.success("用户状态修改成功",null);
    }

    @Override
    public Result<String> updateAvatar(Long id, MultipartFile file) {
        Result<String> uploadResult=fileService.upload(file,"avatar");
        if (uploadResult.getCode() != 200){
            return Result.fail(400, "头像上传失败");
        }
        String avatarUrl = uploadResult.getData();
        User user = userMapper.selectById(id);
        String filename = user.getAvatarUrl();
        fileService.deleteFile(filename);
        user.setAvatarUrl(avatarUrl);
        userMapper.updateById(user);
        return Result.success("头像上传成功！",avatarUrl);
    }

    @Override
    public Result<UserResponseDTO> getUserId(String id, HttpServletRequest request) {
        User user = userMapper.selectById(id);
        if (user == null) return Result.fail(404, "用户不存在");
        UserResponseDTO dto = new UserResponseDTO();
        BeanUtils.copyProperties(user, dto);
        return Result.success("获取成功", dto);
    }
}
