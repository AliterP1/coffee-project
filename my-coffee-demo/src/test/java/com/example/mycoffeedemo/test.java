package com.example.mycoffeedemo;

import com.example.mycoffeedemo.utils.CodeUtil;
import com.example.mycoffeedemo.utils.RedisUtil;
import org.junit.jupiter.api.Test;


public class test {
    @Test
    public void test(){
        CodeUtil codeUtil = new CodeUtil();
        System.out.println(codeUtil.generateCode());
    }
}
