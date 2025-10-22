package com.example.mycoffeedemo.aspect;


import com.example.mycoffeedemo.annotation.RequireOwner;
import com.example.mycoffeedemo.entity.Order;
import com.example.mycoffeedemo.entity.User;
import com.example.mycoffeedemo.mapper.OrderMapper;
import com.example.mycoffeedemo.mapper.UserMapper;
import com.example.mycoffeedemo.utils.CurrentUserUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Objects;

@Aspect
@Component
@RequiredArgsConstructor
public class RequireOwnerAspect {

    private final HttpServletRequest request;
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;

    @Before("@annotation(requireOwner)")
    public void checkOwnership(JoinPoint joinPoint, RequireOwner requireOwner) {
        Long currentUserId = CurrentUserUtil.getCurrentUserId(request);
        String role = CurrentUserUtil.getCurrentUserRole(request);

        if (currentUserId == null) {
            throw new SecurityException("未登录");
        }

        // 角色白名单直接放行
        if (requireOwner.allowRoles().length > 0 && Arrays.asList(requireOwner.allowRoles()).contains(role)) {
            return;
        }

        // 找到方法参数里的资源 ID
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        Object[] args = joinPoint.getArgs();

        Long resourceId = null;
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getName().equals(requireOwner.idArg())) {
                resourceId = Long.valueOf(args[i].toString());
                break;
            }
        }
        if (resourceId == null) {
            throw new SecurityException("找不到资源 ID 参数: " + requireOwner.idArg());
        }

        // 根据资源类型进行校验
        switch (requireOwner.resource()) {
            case "user":
                User user = userMapper.selectById(resourceId);
                if (user == null || !Objects.equals(user.getId(), currentUserId)) {
                    throw new SecurityException("无权访问他人信息");
                }
                break;

            case "order":
                Order order = orderMapper.selectById(resourceId);
                if (order == null || !Objects.equals(order.getUserId(), currentUserId)) {
                    throw new SecurityException("无权查看该订单");
                }
                break;

            default:
                throw new IllegalArgumentException("未知资源类型: " + requireOwner.resource());
        }
    }
}
