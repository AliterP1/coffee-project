package com.example.mycoffeedemo;

import com.example.mycoffeedemo.entity.User;
import com.example.mycoffeedemo.utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MyCoffeeDemoApplicationTests {

    @Autowired
    private RedisUtil  redisUtil;
    @Test
    void contextLoads() {
        User user = new User();
        user.setId(1L);
        user.setUsername("罗佳俊");
        user.setPassword("123456");
        user.setPhone("18281190805");
//        System.out.println(redisTemplate.opsForValue().get("name"));
        redisUtil.set("user:"+user.getId(), user);
        redisUtil.expire("user:"+user.getId(), 30);
        System.out.println(redisUtil.get("user:" + user.getId()));
        System.out.println(redisUtil.getExpire("user:"+user.getId()));
    }
}
