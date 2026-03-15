package com.example.mycoffeedemo.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mycoffeedemo.dto.OrderResponseDTO;
import com.example.mycoffeedemo.dto.UserRequestDTO;
import com.example.mycoffeedemo.dto.UserResponseDTO;
import com.example.mycoffeedemo.entity.Order;
import com.example.mycoffeedemo.entity.User;
import com.example.mycoffeedemo.mapper.UserMapper;
import com.example.mycoffeedemo.service.FileService;
import com.example.mycoffeedemo.service.UserService;
import com.example.mycoffeedemo.utils.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import static com.example.mycoffeedemo.utils.DataUtils.SMS_CODE_KEY;
import static com.example.mycoffeedemo.utils.DataUtils.SMS_LIMIT_KEY;


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private FileService fileService;

    @Autowired
    private RedisUtil redisUtil;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Result<UserResponseDTO> login(UserRequestDTO dto) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("email", dto.getEmail()));
        if (user == null) {
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
        String token = jwtUtils.generateToken(dto.getUsername(), claims);
        UserResponseDTO responseDTO = new UserResponseDTO();
        BeanUtils.copyProperties(user, responseDTO);
        responseDTO.setToken(token);

        return Result.success("登录成功！", responseDTO);
    }

    @Override
    public Result<UserResponseDTO> register(UserRequestDTO dto) {
        //1、判断用户是否存在
        User exits = userMapper.selectOne(new QueryWrapper<User>().eq("email", dto.getEmail()));
        if (exits != null) {
            return Result.fail(404, "用户名已存在");
        }
        if (dto.getPhone() != null && !PhoneUtil.isValidPhone(dto.getPhone())) {
            return Result.fail(403, "手机格式错误");
        }
        //2、密码加密
        String password = passwordEncoder.encode(dto.getPassword());

        //3、保存用户
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(password);
        userMapper.insert(user);

        UserResponseDTO responseDTO = new UserResponseDTO();
        BeanUtils.copyProperties(user, responseDTO);
        return Result.success("注册成功", responseDTO);
    }

    @Override
    public Result<Void> changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);

        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        if ("frozen".equals(user.getStatus())) {
            return Result.fail(403, "用户正在冻结，静止修改密码");
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
            return Result.success("密码修改成功", null);
        } else {
            return Result.fail(500, "密码修改失败");
        }
    }

    @Override
    public Result<String> updateStatus(Long userId, String status) {
        //检查status状态合法性
        if (!"active".equals(status) && !"frozen".equals(status) && !"deleted".equals(status)) {
            return Result.fail(400, "非法状态");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
        return Result.success("用户状态修改成功", null);
    }

    @Override
    public Result<String> updateAvatar(Long id, MultipartFile file) {
        Result<String> uploadResult = fileService.upload(file, "avatar");
        if (uploadResult.getCode() != 200) {
            return Result.fail(400, "头像上传失败");
        }
        String avatarUrl = uploadResult.getData();
        User user = userMapper.selectById(id);
        String filename = user.getAvatarUrl();
        fileService.deleteFile(filename);
        user.setAvatarUrl(avatarUrl);
        userMapper.updateById(user);
        return Result.success("头像上传成功！", avatarUrl);
    }

    @Override
    public Result<UserResponseDTO> getUserId(String id, HttpServletRequest request) {
        User user = userMapper.selectById(id);
        if (user == null) return Result.fail(404, "用户不存在");
        UserResponseDTO dto = new UserResponseDTO();
        BeanUtils.copyProperties(user, dto);
        return Result.success("获取成功", dto);
    }

    @Override
    public Result<IPage<UserResponseDTO>> getAllUser(int page, int size) {
        Page<User> userParam = new Page<>(page, size);
        IPage<User> userPage = userMapper.selectPage(userParam, new LambdaQueryWrapper<User>());
        IPage<UserResponseDTO> dtoPage = userPage.convert(user -> {
            UserResponseDTO dto = new UserResponseDTO();
            BeanUtils.copyProperties(user, dto);
            return dto;
        });
        return Result.success(dtoPage);
    }

    @Override
    public Result<UserResponseDTO> updateUser(Long id, UserRequestDTO dto) {

        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }

        if ("deleted".equals(user.getStatus())) {
            return Result.fail(403, "用户已被删除");
        }
        BeanUtils.copyProperties(dto, user);

        userMapper.updateById(user);

        UserResponseDTO responseDTO = new UserResponseDTO();
        BeanUtils.copyProperties(user, responseDTO);


        return Result.success("用户信息更新成功", responseDTO);
    }

    @Override
    public Result<String> resetPassword(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }

        if ("deleted".equals(user.getStatus())) {
            return Result.fail(403, "用户已删除，无法重置密码");
        }

        // 初始化密码 = 邮箱 + 123
        String initPassword = user.getEmail() + "123";

        // 加密
        String encodedPassword = passwordEncoder.encode(initPassword);
        user.setPassword(encodedPassword);

        userMapper.updateById(user);

        return Result.success("密码已重置为：邮箱+123", null);
    }

    @Override
    public Result<UserResponseDTO> addUser(UserRequestDTO dto) {
        // 校验邮箱是否已存在
        User exist = userMapper.selectOne(
                new QueryWrapper<User>().eq("email", dto.getEmail())
        );
        if (exist != null) {
            return Result.fail(400, "邮箱已存在");
        }

        // 校验手机号格式
        if (dto.getPhone() != null && !PhoneUtil.isValidPhone(dto.getPhone())) {
            return Result.fail(400, "手机号格式错误");
        }

        // 构建用户对象
        User user = new User();

        // 只复制安全字段（不要直接 copy 全部）
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());

        // 设置默认状态
        user.setStatus("active");

        // 设置默认角色
        if (dto.getRole() == null || "admin".equals(dto.getRole())) {
            user.setRole("user");   // 防止创建管理员
        } else {
            user.setRole(dto.getRole());
        }

        // 密码处理
        String rawPassword = dto.getEmail()+ "123";
        if (rawPassword.length() < 6) {
            return Result.fail(400, "密码至少6位");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);

        // 7️⃣ 保存数据库
        userMapper.insert(user);

        // 8️⃣ 返回 DTO
        UserResponseDTO responseDTO = new UserResponseDTO();
        BeanUtils.copyProperties(user, responseDTO);

        return Result.success("用户创建成功", responseDTO);
    }

    @Override
    public Result<String> codePhone(String phone) {
        // 1 手机号简单校验
        if(phone == null || !phone.matches("^1[3-9]\\d{9}$")){
            return Result.fail("手机号格式错误");
        }
        // 2 限制发送频率（60秒）
        String limitKey = SMS_LIMIT_KEY + phone;
        if(redisUtil.hasKey(limitKey)){
            return Result.fail("发送过于频繁，请稍后再试");
        }
        // 3 生成验证码
        String code = CodeUtil.generateCode();
        // 4 保存验证码到 Redis，5分钟过期
        String codeKey = SMS_CODE_KEY + phone;
        boolean saveCode = redisUtil.set(codeKey, code, 300);
        if (!saveCode) {
            return Result.fail("验证码保存失败，请稍后重试！");
        }
        // 5. 保存限流标记，60秒过期
        boolean saveLimit = redisUtil.set(limitKey, "1", 60);
        if (!saveLimit) {
            return Result.fail("短信发送失败，请稍后重试！");
        }
        // 6. 调用短信平台发送验证码
        // 这里先模拟，正式环境替换成阿里云/腾讯云短信接口
        System.out.println("手机号：" + phone + "，验证码：" + code);
        return Result.success("短信发送成功！");
    }

    @Override
    public Result<UserResponseDTO> phoneLogin(String phone, String code) {
        Object phoneCode = redisUtil.get(SMS_CODE_KEY + phone);

        if (phoneCode == null) {
            return Result.fail(400, "验证码已过期或未发送成功");
        }
        if (!code.equals(phoneCode.toString())) {
            return Result.fail(400, "验证码错误");
        }
        // 删除验证码
        redisUtil.del(SMS_CODE_KEY + phone);
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("phone", phone));

        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        if ("frozen".equals(user.getStatus())) {
            return Result.fail(403, "用户已被冻结");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());
        claims.put("email", user.getEmail());
        String token = jwtUtils.generateToken(user.getUsername(), claims);
        UserResponseDTO responseDTO = new UserResponseDTO();
        BeanUtils.copyProperties(user, responseDTO);
        responseDTO.setToken(token);
        return Result.success("登录成功！", responseDTO);
    }

}
