package com.phenikaa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
})
@EnableFeignClients
public class ProfileServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProfileServiceApplication.class, args);
    }
}
