package com.phenikaa;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SubmissionServiceApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(SubmissionServiceApplication.class, args);
    }
}
