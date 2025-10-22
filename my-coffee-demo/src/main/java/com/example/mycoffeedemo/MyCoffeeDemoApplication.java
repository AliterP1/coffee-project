package com.example.mycoffeedemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MyCoffeeDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyCoffeeDemoApplication.class, args);
    }

}
