package com.example.mycoffeedemo.config;

import com.example.mycoffeedemo.utils.JwtUtils;
import com.example.mycoffeedemo.utils.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils  jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String token = request.getHeader("Authorization");
        // token 不存在 或 不合法
        if (token == null || !token.startsWith("Bearer ")) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            Result<Object> result = Result.fail(401, "未登录,请先登录");
            response.getWriter().write(new ObjectMapper().writeValueAsString(result));
            return false;
        }

        token = token.substring(7); // 去掉 "Bearer "

        // 验证 token
        if (!jwtUtils.validateToken(token)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            Result<Object> result = Result.fail(401, "Token过期,请重新登录");
            response.getWriter().write(new ObjectMapper().writeValueAsString(result));
            return false;
        }

        // 放入 user，供 Controller 使用
        String username = jwtUtils.getUsernameFromToken(token);
        String role = jwtUtils.getRoleFromToken(token);
        Long  userId = jwtUtils.getUserIdFromToken(token);
        String email = jwtUtils.getEmailFromToken(token);

        request.setAttribute("username", username);
        request.setAttribute("role", role);
        request.setAttribute("userId", userId);
        request.setAttribute("email", email);

        return true;
    }
}
