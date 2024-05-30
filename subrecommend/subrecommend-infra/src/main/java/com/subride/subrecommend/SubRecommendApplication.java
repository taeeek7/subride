package com.subride.subrecommend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients //FeignClient 사용 시 반드시 활성화 해야 함
public class SubRecommendApplication {
    public static void main(String[] args) {
        SpringApplication.run(SubRecommendApplication.class, args);
    }
}
