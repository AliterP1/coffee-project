package com.example.mycoffeedemo.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private  String jwtSecret;

    @Value("${jwt.expiration}")
    private long expiration;

    private  SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * 生成 token
     * */

    public  String generateToken(String username,Map<String,Object> claims) {
        return Jwts.builder()
                .setSubject(username)
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+expiration))
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    /**
     * 校验 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    /**
     * 从 Token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }
    /**
     * 获取role的快捷方法
     * */

    public String getRoleFromToken(String token) {
        return parseClaims(token).get("role",String.class).toString();
    }

    /**
     * 获取userId的快捷方法
     * */
    public  Long getUserIdFromToken(String token) {
        return Long.valueOf(parseClaims(token).get("userId").toString());
    }

    /**
     * 获取Email的快捷方法
     * */
    public  String getEmailFromToken(String token) {
        return parseClaims(token).get("email",String.class).toString();
    }
    /**
     * 解析 Claims
     */
    private  Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

