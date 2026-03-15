package com.example.mycoffeedemo.utils;


import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class CodeUtil {
    public static String generateCode() {
        Random random = new Random();
        int num = random.nextInt(1000000);
        return String.format("%06d", num);
    }
}
