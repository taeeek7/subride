package com.subride.mysub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MySubApplication {
    public static void main(String[] args) {
        SpringApplication.run(MySubApplication.class, args);
    }
}