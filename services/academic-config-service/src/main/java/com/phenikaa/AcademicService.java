package com.phenikaa;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class AcademicService {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(AcademicService.class, args);
    }
}
