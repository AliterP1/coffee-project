package com.example.mycoffeedemo.utils;

import java.util.regex.Pattern;

public class PhoneUtil {
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";


    public static boolean isValidPhone(String phone){
        return Pattern.matches(PHONE_REGEX,phone);
    }
}
